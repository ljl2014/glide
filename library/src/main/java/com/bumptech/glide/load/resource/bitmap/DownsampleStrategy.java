package com.bumptech.glide.load.resource.bitmap;

/**
 * Indicates the algorithm to use when downsampling images.
 */
public abstract class DownsampleStrategy {

  /**
   * Scales, maintaining the original aspect ratio, so that one of the image's dimensions is
   * exactly equal to the requested size and the other dimension is less than or equal to the
   * requested size.
   *
   * <p>This method will upscale if the requested width and height are greater than the source width
   * and height. To avoid upscaling, use {@link #AT_LEAST} or {@link #AT_MOST}.
   */
  public static final DownsampleStrategy CENTER_INSIDE = new CenterInside();

  /**
   * Scales, maintaining the original aspect ratio, so that one of the image's dimensions is
   * exactly equal to the requested size and the other dimension is greater than or equal to
   * the requested size.
   *
   * <p>This method will upscale if the requested width and height are greater than the source width
   * and height. To avoid upscaling, use {@link #AT_LEAST} or {@link #AT_MOST}.
   */
  public static final DownsampleStrategy CENTER_OUTSIDE = new CenterOutside();

  /**
   * Downsamples so the image's smallest dimension is between the given dimensions and 2x the given
   * dimensions, with no size restrictions on the image's largest dimension.
   */
  public static final DownsampleStrategy AT_LEAST = new AtLeast();

  /**
   * Downsamples so the image's largest dimension is between 1/2 the given dimensions and the given
   * dimensions, with no restrictions on the image's smallest dimension.
   */
  public static final DownsampleStrategy AT_MOST = new AtMost();

  /**
   * Performs no downsampling or scaling.
   */
  public static final DownsampleStrategy NONE = new None();

  /**
   * Default strategy, currently {@link #AT_LEAST}.
   */
  public static final DownsampleStrategy DEFAULT = AT_LEAST;

  /**
   * Returns a float (0, +infinity) indicating a scale factor to apply to the source
   * width and height when displayed in the requested width and height.
   *
   * <p>The returned scale factor will be split into a power of two sample size applied via
   * {@link android.graphics.BitmapFactory.Options#inSampleSize} and a float scale factor applied
   * after downsampling via {@link android.graphics.BitmapFactory.Options#inTargetDensity} and
   * {@link android.graphics.BitmapFactory.Options#inDensity}. Because of rounding errors the scale
   * factor may not be applied precisely.
   *
   * @param sourceWidth   The width in pixels of the image to be downsampled.
   * @param sourceHeight  The height in pixels of the image to be downsampled.
   * @param requestedWidth  The width in pixels of the view/target the image will be displayed in.
   * @param requestedHeight The height in pixels of the view/target the image will be displayed in.
   */
  public abstract float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth,
      int requestedHeight);

  private static class CenterInside extends DownsampleStrategy {

    @Override
    public float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth,
        int requestedHeight) {
      float widthPercentage = requestedWidth / (float) sourceWidth;
      float heightPercentage = requestedHeight / (float) sourceHeight;
      return Math.min(widthPercentage, heightPercentage);
    }
  }

  private static class CenterOutside extends DownsampleStrategy {
     @Override
    public float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth,
        int requestedHeight) {
      float widthPercentage = requestedWidth / (float) sourceWidth;
      float heightPercentage = requestedHeight / (float) sourceHeight;
      return Math.max(widthPercentage, heightPercentage);
    }
  }

  private static class AtLeast extends DownsampleStrategy {

    @Override
    public float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth,
        int requestedHeight) {
      int minIntegerFactor = Math.min(sourceHeight / requestedHeight, sourceWidth / requestedWidth);
      return minIntegerFactor == 0 ? 1 : Integer.highestOneBit(minIntegerFactor);
    }
  }

  private static class AtMost extends DownsampleStrategy {
    @Override
    public float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth,
        int requestedHeight) {
      int maxMultiplier = (int) Math.ceil(Math.max(sourceHeight / (float) requestedHeight,
          sourceWidth / (float) requestedWidth));
      if (maxMultiplier <= 1) {
        return 1;
      } else {
        int highestOneBit = Integer.highestOneBit(maxMultiplier);
        return highestOneBit << (maxMultiplier == highestOneBit ? 0 : 1);
      }
    }
  }

  private static class None extends DownsampleStrategy {
     @Override
    public float getScaleFactor(int sourceWidth, int sourceHeight, int requestedWidth,
        int requestedHeight) {
      return 1f;
    }
  }
}