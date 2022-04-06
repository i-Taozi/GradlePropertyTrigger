package com.stanfy.enroscar.images.views;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.images.ImageConsumer;
import com.stanfy.enroscar.images.ImageRequest;
import com.stanfy.enroscar.images.ImagesManager;
import com.stanfy.enroscar.images.ViewImageConsumer;
import com.stanfy.enroscar.images.ViewImageConsumerFactory;

/**
 * Image holders.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(ImagesManager.IMAGE_CONSUMER_FACTORY_NAME)
public final class ImageConsumers implements ViewImageConsumerFactory {

  @Override
  public ImageConsumer createConsumer(View view) {
    if (view instanceof LoadableImageView) { return new LoadableImageViewConsumer((LoadableImageView)view); }
    if (view instanceof ImageView) { return new ImageViewConsumer((ImageView)view); }
    if (view instanceof CompoundButton) { return new CompoundButtonConsumer((CompoundButton)view); }
    if (view instanceof TextView) { return new TextViewConsumer((TextView)view); }
    return null;
  }
  
  /**
   * Image holder for {@link ImageView}.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  static class ImageViewConsumer extends ViewImageConsumer<ImageView> {
    public ImageViewConsumer(final ImageView view) { super(view); }
    @Override
    public void setImage(final Drawable d, final boolean animate) { getView().setImageDrawable(d); }
  }

  /**
   * Image holder for {@link ImageView}.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  static class LoadableImageViewConsumer extends ImageViewConsumer {

    public LoadableImageViewConsumer(final LoadableImageView view) {
      super(view);
    }

    @Override
    protected void prepareImageRequest(final ImageRequest request) {
      super.prepareImageRequest(request);
      request.setSkipScaleBeforeMemCache(((LoadableImageView)getView()).isSkipScaleBeforeCache());
    }

    @Override
    public boolean allowSmallImagesFromCache() {
      return ((LoadableImageView)getView()).isAllowSmallImagesInCache();
    }

    @Override
    public boolean skipLoadingImage() {
      return ((LoadableImageView)getView()).isSkipLoadingImage();
    }

    @Override
    public Drawable getLoadingImage() {
      return ((LoadableImageView)getView()).getLoadingImage();
    }

    @Override
    public void setImage(final Drawable d, final boolean animate) {
      final LoadableImageView view = (LoadableImageView)this.getView();
      if (animate && view.isUseTransition()) {
        view.setImageDrawableWithTransition(d, view.isTransitionCrossfade());
      } else {
        view.setImageDrawable(d);
      }
    }

    @Override
    public void setLoadingImage(final Drawable d) {
      final LoadableImageView view = (LoadableImageView)getView();
      view.setImageDrawable(d);
      view.setTemporaryScaleType(ScaleType.FIT_XY);
    }

  }

  /**
   * Image holder for {@link CompoundButton}.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  static class CompoundButtonConsumer extends ViewImageConsumer<CompoundButton> {

    public CompoundButtonConsumer(final CompoundButton view) {
      super(view);
    }

    @Override
    public void setImage(final Drawable d, final boolean animate) {
      getView().setButtonDrawable(d);
    }

  }

  /**
   * Image consumer for {@link TextView}. Sets loaded image as a left drawable.
   * @author Olexandr Tereshchuk - Stanfy (http://www.stanfy.com)
   */
  static class TextViewConsumer extends ViewImageConsumer<TextView> {

    public TextViewConsumer(final TextView view) {
      super(view);
    }

    @Override
    public void setImage(final Drawable d, final boolean animate) {
      TextView view = getView();
      if (view instanceof LoadableTextView) {
        ((LoadableTextView)view).setLoadedDrawable(d);
      } else {
        view.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
      }
    }

    @Override
    protected int getTargetHeight() {
      return -1;
    }

    @Override
    protected int getTargetWidth() {
      return -1;
    }

  }

}
