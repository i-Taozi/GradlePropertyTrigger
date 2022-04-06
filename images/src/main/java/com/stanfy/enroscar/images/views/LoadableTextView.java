package com.stanfy.enroscar.images.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.TextView;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.images.ImagesManager;
import com.stanfy.enroscar.images.R;

/**
 * Text view that can load a remote images and use it as comppound drawables.
 * Early implementation that supports only one drawble(drawableLeft).
 * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class LoadableTextView extends TextView {

  /** Image URI. */
  private Uri imageUri;
  /** Images manager. */
  private ImagesManager imagesManager;

  /** Loadable drawable width/height. */
  private int loadableDrawableWidth, loadableDrawableHeight;

  /** Loadable drawable position. */
  private LoadableImagePosition loadableDrawablePosition;

  /**
   * Loadable drawable positions.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static enum LoadableImagePosition {
    /** Positions. */
    LEFT, TOP, RIGHT, BOTTOM;
  }

  public LoadableTextView(final Context context) {
    this(context, null);
  }

  public LoadableTextView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public LoadableTextView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  private void init(final Context context, final AttributeSet attrs, final int defStyle) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadableTextView, defStyle, 0);
    setLoadableDrawableWidth(a.getDimensionPixelSize(R.styleable.LoadableTextView_loadableDrawableWidth, -1));
    setLoadableDrawableHeight(a.getDimensionPixelSize(R.styleable.LoadableTextView_loadableDrawableHeight, -1));
    final int pos = a.getInt(R.styleable.LoadableTextView_loadableDrawablePosition, 0);
    a.recycle();

    setLoadableDrawablePosition(LoadableImagePosition.values()[pos]);
    if (loadableDrawablePosition != LoadableImagePosition.LEFT) { // reset drawables
      final Drawable[] drawables = getCompoundDrawables();
      setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    this.imagesManager = BeansManager.get(context).getContainer().getBean(ImagesManager.class);
  }

  /** @param loadableDrawablePosition the loadableDrawablePosition to set */
  public void setLoadableDrawablePosition(final LoadableImagePosition loadableDrawablePosition) {
    this.loadableDrawablePosition = loadableDrawablePosition;
  }

  /** @param loadableDrawableWidth the loadableDrawableWidth to set */
  public void setLoadableDrawableWidth(final int loadableDrawableWidth) {
    this.loadableDrawableWidth = loadableDrawableWidth;
  }

  /** @param loadableDrawableHeight the loadableDrawableHeight to set */
  public void setLoadableDrawableHeight(final int loadableDrawableHeight) {
    this.loadableDrawableHeight = loadableDrawableHeight;
  }

  /**
   * Sets remote image to be downloaded.
   * @param uri image URI to load
   */
  public void setLoadableDrawable(final Uri uri) {
    if (imageUri != null && imageUri.equals(uri)) { return; }
    imageUri = uri;
    if (imagesManager != null) {
      imagesManager.populateImage(this, uri != null ? uri.toString() : null);
    }
  }

  public void setLoadedDrawable(final Drawable d) {
    final Drawable[] drawables = getCompoundDrawables();
    drawables[loadableDrawablePosition.ordinal()] = d;
    setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
  }

  @Override
  public void setCompoundDrawables(final Drawable left, final Drawable top, final Drawable right, final Drawable bottom) {
    if (loadableDrawablePosition != null) { // it's possible during super.init
      final int lw = loadableDrawableWidth, lh = loadableDrawableHeight;
      final Drawable d;
      switch (loadableDrawablePosition) {
      case LEFT: d = left; break;
      case TOP: d = top; break;
      case RIGHT: d = right; break;
      case BOTTOM: d = bottom; break;
      default: throw new IllegalStateException("Unknown position " + loadableDrawablePosition);
      }
      if (d != null && lw != -1 && lh != -1) { d.setBounds(0, 0, lw, lh); }
    }
    super.setCompoundDrawables(left, top, right, bottom);
  }

  private void cancelLoading() {
    if (imageUri != null && imagesManager != null) {
      imagesManager.cancelImageLoading(this);
      imageUri = null;
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
