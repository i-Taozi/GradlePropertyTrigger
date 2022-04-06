/**
 *
 */
package com.stanfy.enroscar.images.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.stanfy.enroscar.beans.Bean;
import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.images.ImagesManager;
import com.stanfy.enroscar.net.cache.BaseFileResponseCache;
import com.stanfy.enroscar.net.cache.CacheEntry;
import com.stanfy.enroscar.net.cache.CacheTimeRule;

/**
 * File-based cache used by images manager.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(value = ImagesManager.CACHE_BEAN_NAME, contextDependent = true)
public class ImageFileCache extends BaseFileResponseCache implements Bean {

  /** Default images cache size (10M). */
  public static final long MAX_SIZE = 10 * 1024 * 1024;

  /** Application context. */
  private final Context context;
  
  public ImageFileCache(final Context context) {
    this.context = context;
  }
  
  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
    if (getWorkingDirectory() == null) {
      final String eState = Environment.getExternalStorageState();
      File baseDir = Environment.MEDIA_MOUNTED.equals(eState)
          ? context.getExternalCacheDir()
          : context.getCacheDir();
      if (baseDir == null) {
        baseDir = context.getCacheDir();
        Log.w(TAG, "Could not locate cache on the external storage");
      }
      setWorkingDirectory(new File(baseDir, "images"));
    }
    if (getMaxSize() == 0) {
      setMaxSize(MAX_SIZE);
    }
    super.onInitializationFinished(beansContainer);
  }
  
  @Override
  protected CacheEntry createCacheEntry() { return new ImageCacheEntry(); }

  /** Image cache entry. */
  public static class ImageCacheEntry extends CacheEntry {
    /** Image type identifier. */
    int imageType = -1;

    @Override
    protected void writeMetaData(final Writer writer) throws IOException {
      writeInt(writer, imageType);
    }

    @Override
    protected void readMetaData(final InputStream in) throws IOException {
      imageType = readInt(in);
    }

  }

  /** Cache rule. */
  public static class ImageTypeBasedCacheRule extends CacheTimeRule {
    /** Image type identifier. */
    final int imageType;

    public ImageTypeBasedCacheRule(final int imageType, final long time) {
      super(time);
      this.imageType = imageType;
    }

    @Override
    public boolean matches(final CacheEntry cacheEntry) {
      return cacheEntry instanceof ImageCacheEntry && imageType == ((ImageCacheEntry)cacheEntry).imageType;
    }

    @Override
    protected String matcherToString() {
      return "imageType=" + imageType;
    }

  }

  /** 'Until' cache rule. */
  public static class ImageTypeBasedUntilCacheRule extends ImageTypeBasedCacheRule {

    public ImageTypeBasedUntilCacheRule(final int imageType, final long time) {
      super(imageType, time);
    }

    @Override
    public boolean isActual(final long createTime) { return isUntilActual(createTime, getTime()); }

  }

}
