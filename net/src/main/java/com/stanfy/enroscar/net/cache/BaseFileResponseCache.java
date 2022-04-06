package com.stanfy.enroscar.net.cache;

import android.os.AsyncTask;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.DestroyingBean;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.io.BuffersPool;
import com.stanfy.enroscar.io.IoUtils;
import com.stanfy.enroscar.net.UrlConnectionWrapper;
import com.stanfy.enroscar.net.cache.CacheEntry.CacheEntryListener;
import com.stanfy.enroscar.net.cache.CacheEntry.CacheEntryRequest;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for cache implementations based on file system.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public abstract class BaseFileResponseCache extends BaseSizeRestrictedCache
    implements EnhancedResponseCache, CacheEntryListener, DestroyingBean, InitializingBean {

  /** Cache entry index. */
  private static final int ENTRY_BODY = 0, ENTRY_METADATA = 1;
  /** Count of cache entries. */
  private static final int ENTRIES_COUNT = 2;

  /** Logging tag. */
  protected static final String TAG = "FileCache";
  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_NET_CACHE;

  /** Application version. */
  private static final int VERSION = 20120718;

  /** Disk cache instance. */
  private DiskLruCache diskCache;

  /** Buffers pool. */
  private BuffersPool buffersPool;

  /** Sync point for cache installation. */
  private final CountDownLatch initSync = new CountDownLatch(1);
  
  /** Statistics. */
  private final AtomicInteger writeSuccessCount = new AtomicInteger(0),
                              writeAbortCount = new AtomicInteger(0),
                              hitCount = new AtomicInteger(0);

  /**
   * Setup cache. This operation causes disk reads.
   * @param version cache version
   * @throws IOException if error happens
   */
  protected void install(final int version) throws IOException {
    if (buffersPool == null) {
      throw new IllegalStateException("Buffers pool is not resolved");
    }
    
    diskCache = DiskLruCache.open(ensureWorkingDirectory(), version, ENTRIES_COUNT, getMaxSize());
    onCacheInstalled();
  }

  // this method is synchronized in order to avoid concurrent calls to mkdir
  private synchronized File ensureWorkingDirectory() throws IOException {
    File directory = getWorkingDirectory();
    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        throw new IOException("Working directory " + directory + " cannot be created");
      }
    } else {
      if (!directory.isDirectory()) {
        if (!directory.delete()) {
          throw new IOException(directory + " is not a directory and cannot be deleted");
        }
        if (!directory.mkdirs()) {
          throw new IOException("Working directory " + directory + " cannot be recreated");
        }
      }
    }
    return directory;
  }
  
  /**
   * Called when cache is initialized before any reads or writes to this cache. 
   * Called from a working thread. 
   */
  protected void onCacheInstalled() {
    // nothing
  }
  
  public void delete() throws IOException {
    if (DEBUG) { Log.d(TAG, "Delete cache workingDirectory=" + diskCache.getDirectory()); }
    diskCache.delete();
  }

  public DiskLruCache getDiskCache() { return diskCache; }

  private DiskLruCache.Snapshot readCacheInfo(final CacheEntry requestInfo, final CacheEntry entry) {
    if (!checkDiskCache()) { return null; }

    final String key = requestInfo.getCacheKey();

    DiskLruCache.Snapshot snapshot;
    InputStream bufferedStream = null;
    try {
      snapshot = diskCache.get(key);
      if (snapshot == null) {
        return null;
      }
      bufferedStream = buffersPool.bufferize(snapshot.getInputStream(ENTRY_METADATA));
      entry.readFrom(bufferedStream);
    } catch (final IOException e) {
      IoUtils.closeQuietly(bufferedStream);
      // Give up because the cache cannot be read.
      return null;
    }
    return snapshot;
  }

  /**
   * Read cache for the specified cache entry.
   * @param requestInfo request info (cache key)
   * @return cache response instance
   */
  protected CacheResponse get(final CacheEntry requestInfo) {
    if (!checkDiskCache()) { return null; }
    final CacheEntry entry = newCacheEntry();
    final DiskLruCache.Snapshot snapshot = readCacheInfo(requestInfo, entry);
    if (snapshot == null) { return null; }

    if (!entry.matches(requestInfo) || !entry.canBeUsed()) {
      snapshot.close();
      return null;
    }

    hitCount.incrementAndGet();

    final InputStream body = newBodyInputStream(snapshot);
    return entry.newCacheResponse(body);
  }

  @Override
  public final CacheResponse get(final URI uri, final URLConnection connection) throws IOException {
    final CacheEntry requestInfo = newCacheEntry();
    requestInfo.setFrom(connection);
    return get(requestInfo);
  }

  @Override
  public final CacheResponse get(final URI uri, final String requestMethod, final Map<String, List<String>> requestHeaders) {
    final CacheEntry requestInfo = newCacheEntry();
    requestInfo.set(uri, requestMethod, requestHeaders);
    return get(requestInfo);
  }

  /**
   * Override this method in order to provide custom {@link CacheEntry} implementation.
   * But use {@link #newCacheEntry()} method for creating ones.  
   * @return new cache entry instance
   */
  protected abstract CacheEntry createCacheEntry();

  /**
   * Create and setup new cache entry.
   * @return new cache entry instance
   */
  protected final CacheEntry newCacheEntry() {
    final CacheEntry result = createCacheEntry();
    result.setListener(this);
    return result;
  }

  /**
   * Returns an input stream that reads the body of a snapshot, closing the
   * snapshot when the stream is closed.
   */
  private InputStream newBodyInputStream(final DiskLruCache.Snapshot snapshot) {
    return new FilterInputStream(snapshot.getInputStream(ENTRY_BODY)) {
      @Override public void close() throws IOException {
        snapshot.close();
        super.close();
      }
    };
  }

  @Override
  public CacheRequest put(final URI uri, final URLConnection connection) throws IOException {
    if (!checkDiskCache()) { return null; }
    final URLConnection urlConnection = UrlConnectionWrapper.unwrap(connection);

    final CacheEntry cacheEntry = newCacheEntry();
    cacheEntry.setFrom(urlConnection);
    cacheEntry.setResponseData(urlConnection);

    if (!cacheEntry.canBeCached()) { return null; }

    final String key = cacheEntry.getCacheKey();

    DiskLruCache.Editor editor = null;
    OutputStream metaOut = null;
    try {
      editor = diskCache.edit(key);
      if (editor == null) {
        return null;
      }
      metaOut = buffersPool.bufferize(editor.newOutputStream(ENTRY_METADATA));
      cacheEntry.writeTo(metaOut);
      return cacheEntry.newCacheRequest(buffersPool.bufferize(editor.newOutputStream(ENTRY_BODY)), editor);
    } catch (final IOException e) {
      Log.w(TAG, "Cannot write cache entry", e);
      // Give up because the cache cannot be written.
      try {
        if (editor != null) {
          editor.abort();
        }
      } catch (final IOException ignored) {
        Log.w(TAG, "Cannot abort editor", e);
      }
      return null;
    } finally {
      IoUtils.closeQuietly(metaOut);
    }

  }

  private boolean checkDiskCache() {
    try {
      initSync.await();
    } catch (InterruptedException e) {
      Log.i(TAG, "Init sync waiting was interrupted for cache " + this);
    }
    
    if (diskCache == null || diskCache.isClosed()) {
      Log.e(TAG, "File cache is being used but not properly installed, diskCache = " + diskCache);
      return false;
    }
    return true;
  }

  private CacheEntry createGetEntry(final String url) {
    try {
      if (url == null) { throw new URISyntaxException("<null uri>", "URI is null"); }
      final CacheEntry cacheEntry = newCacheEntry();
      cacheEntry.set(new URI(url), "GET", Collections.<String, List<String>>emptyMap());
      return cacheEntry;
    } catch (final URISyntaxException e) {
      Log.e(TAG, "Bad url " + url + ", cannot deleteGetEntry", e);
      return null;
    }
  }

  @Override
  public boolean deleteGetEntry(final String url) throws IOException {
    if (!checkDiskCache()) { return false; }
    final CacheEntry cacheEntry = createGetEntry(url);
    if (cacheEntry == null) { return false; }
    return diskCache.remove(cacheEntry.getCacheKey());
  }

  @Override
  public boolean contains(final String url) {
    final CacheEntry requestInfo = createGetEntry(url);
    if (requestInfo == null) { return false; }

    final CacheEntry entry = newCacheEntry();
    final DiskLruCache.Snapshot snapshot = readCacheInfo(requestInfo, entry);
    if (snapshot == null) { return false; }
    IoUtils.closeQuietly(snapshot);

    return entry.matches(requestInfo);
  }

  @Override
  public String getLocalPath(final String url) {
    final CacheEntry requestInfo = createGetEntry(url);
    if (requestInfo == null) { return null; }
    File f = new File(getWorkingDirectory(), requestInfo.getCacheKey() + "." + ENTRY_BODY);
    return f.getAbsolutePath();
  }

  @Override
  public void onCacheEntryWriteAbort(final CacheEntryRequest request) {
    writeAbortCount.incrementAndGet();
  }
  @Override
  public void onCacheEntryWriteSuccess(final CacheEntryRequest request) {
    writeSuccessCount.incrementAndGet();
    try {
      diskCache.flush();
    } catch (final IOException e) {
      Log.w(TAG, "Cannot flush disk cache", e);
    }
  }

  public int getWriteSuccessCount() { return writeSuccessCount.get(); }
  public int getWriteAbortCount() { return writeAbortCount.get(); }
  public int getHitCount() { return hitCount.get(); }

  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
    this.buffersPool = beansContainer.getBean(BuffersPool.class.getName(), BuffersPool.class);
    if (buffersPool == null) {
      throw new IllegalStateException("Buffers pool must be initialized before the response cache");
    }
    
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(final Void... params) {
        try {
          if (DEBUG) {
            Log.i(TAG, "Install new file cache workingDirectory=" + getWorkingDirectory() + ", version=" + VERSION + ", maxSize=" + getMaxSize());
          }
          install(VERSION);
        } catch (final IOException e) {
          // We do not throw fatal exception: it's a cache app should be able to work without it
          Log.e(TAG, "Cannot install file cache " + BaseFileResponseCache.this + ". It must be configuration error.", e);
        } finally {
          initSync.countDown();
        }
        return null;
      }
    }
    .execute();
  }


  @Override
  public void onDestroy(final BeansContainer beansContainer) {
    try {
      if (DEBUG) {
        Log.i(TAG, "Close file cache workingDirectory=" + getWorkingDirectory());
      }
      diskCache.close();
    } catch (final IOException e) {
      Log.e(TAG, "Cannot close file cache", e);
    }
  }

  @Override
  public String toString() {
    return getClass().getName() + "@" + hashCode() + "[dir=" + getWorkingDirectory() + ", maxSize=" + getMaxSize() + "]";
  }
  
}
