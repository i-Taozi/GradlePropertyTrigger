package com.stanfy.enroscar.images.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.ImagesLoadListener;
import com.stanfy.enroscar.images.ImagesLoadListenerProvider;
import com.stanfy.enroscar.images.ImagesManager;
import com.stanfy.enroscar.images.R;
import com.stanfy.enroscar.images.ViewImageConsumer;

/**
 * Image view that can load a remote image.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LoadableImageView extends ImageView implements ImagesLoadListenerProvider {

  /** Use transition. */
  public static final int USE_TRANSITION_NO = 0, USE_TRANSITION_YES = 1, USE_TRANSITION_CROSSFADE = 2;

  /** Allow small images in cache option. */
  private boolean allowSmallImagesInCache;
  /** Skip scaling before caching flag. */
  private boolean skipScaleBeforeCache;
  /** Skip loading indicator flag.  */
  private boolean skipLoadingImage;
  /** Use transition option. */
  private int useTransition;

  /** Images load listener. */
  private ImagesLoadListener listener;

  /** Image URI. */
  private Uri loadImageUri;
  /** Images manager. */
  private ImagesManager imagesManager;

  /** Loading image. */
  private Drawable loadingImage;


  public LoadableImageView(final Context context) {
    super(context);
  }

  public LoadableImageView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LoadableImageView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs);
  }

  private void init(final Context context, final AttributeSet attrs) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadableImageView);
    final boolean skipCache = a.getBoolean(R.styleable.LoadableImageView_skipScaleBeforeCache, false);
    final boolean skipLoadIndicator = a.getBoolean(R.styleable.LoadableImageView_skipLoadingImage, false);
    final boolean allowSmallCachedImages = a.getBoolean(R.styleable.LoadableImageView_allowSmallImagesInCache, false);
    final Drawable loadingImage = a.getDrawable(R.styleable.LoadableImageView_loadingImage);
    final int useTransition = a.getInt(R.styleable.LoadableImageView_useTransition, USE_TRANSITION_NO);
    a.recycle();

    setAllowSmallImagesInCache(allowSmallCachedImages);
    setSkipScaleBeforeCache(skipCache);
    setSkipLoadingImage(skipLoadIndicator);
    if (loadingImage != null) {
      setLoadingImageDrawable(loadingImage);
    }
    setUseTransitionMode(useTransition);

    if (!isInEditMode()) {
      this.imagesManager = BeansManager.get(context).getContainer().getBean(ImagesManager.class);
    } else if (loadingImage != null) {
      setScaleType(ScaleType.FIT_XY);
      setImageDrawable(loadingImage);
    }
  }

  /** @param mode mode specification (see {@link #USE_TRANSITION_NO}, {@link #USE_TRANSITION_YES}, {@link #USE_TRANSITION_CROSSFADE}) */
  public void setUseTransitionMode(final int mode) {
    this.useTransition = mode;
  }

  /** @return whether this view wants to use transitions */
  public boolean isUseTransition() {
    return this.useTransition != USE_TRANSITION_NO;
  }

  /** @return whether transition must be performed with crossfade option */
  public boolean isTransitionCrossfade() {
    return this.useTransition == USE_TRANSITION_CROSSFADE;
  }

  /** @param skipScaleBeforeCache the skipScaleBeforeCache to set */
  public void setSkipScaleBeforeCache(final boolean skipScaleBeforeCache) {
    this.skipScaleBeforeCache = skipScaleBeforeCache;
  }

  /** @return the skipScaleBeforeCache */
  public boolean isSkipScaleBeforeCache() {
    return skipScaleBeforeCache;
  }

  /** @param skipLoadingImage the skipLoadingImage to set */
  public void setSkipLoadingImage(final boolean skipLoadingImage) {
    this.skipLoadingImage = skipLoadingImage;
  }

  /** @return the skipLoadingImage */
  public boolean isSkipLoadingImage() {
    return skipLoadingImage;
  }

  public void setAllowSmallImagesInCache(final boolean allowSmallImagesInCache) {
    this.allowSmallImagesInCache = allowSmallImagesInCache;
  }
  public boolean isAllowSmallImagesInCache() {
    return allowSmallImagesInCache;
  }
  
  /** @param listener load listener */
  public void setImagesLoadListener(final ImagesLoadListener listener) {
    this.listener = listener;
    final Object tag = getTag();
    if (tag instanceof ViewImageConsumer) { ((ViewImageConsumer<?>) tag).notifyAboutViewChanges(); }
  }

  @Override
  public ImagesLoadListener getImagesLoadListener() {
    return listener;
  }

  /** @param loadingImage the loadingImage to set */
  public void setLoadingImageDrawable(final Drawable loadingImage) {
    if (this.loadingImage != null) { this.loadingImage.setCallback(null); }
    this.loadingImage = loadingImage;
  }

  /** @param loadingImage the loadingImage to set */
  public void setLoadingImageResourceId(final int loadingImage) {
    //noinspection ConstantConditions
    setLoadingImageDrawable(getResources().getDrawable(loadingImage));
  }

  /** @return the loadingImage */
  public Drawable getLoadingImage() {
    return loadingImage;
  }

  @Override
  public void setImageURI(final Uri uri) {
    if (loadImageUri != null && loadImageUri.equals(uri)) { return; }
    loadImageUri = uri;
    if (imagesManager != null) {
      imagesManager.populateImage(this, uri != null ? uri.toString() : null);
      return;
    }
    super.setImageURI(uri);
  }

  private void cancelLoading() {
    if (loadImageUri != null && imagesManager != null) {
      imagesManager.cancelImageLoading(this);
      loadImageUri = null;
    }
  }
  @Override
  public void onStartTemporaryDetach() {
    super.onStartTemporaryDetach();
    cancelLoading();
  }
  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    cancelLoading();
  }

}
