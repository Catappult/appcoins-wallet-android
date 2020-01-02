package com.asfoundation.wallet.widget;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import androidx.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import java.security.MessageDigest;

public class CardHeaderTransformation extends BitmapTransformation {
  private static final byte[] ID_BYTES = "card_header".getBytes(CHARSET);
  private final int radius;
  int margin = 0;

  public CardHeaderTransformation(int radius) {
    this.radius = radius;
  }

  public Bitmap transform(Bitmap toTransform) {
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setShader(new BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

    Bitmap output = Bitmap.createBitmap(toTransform.getWidth(), toTransform.getHeight(),
        Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    canvas.drawRect(new RectF(margin, margin + radius, toTransform.getWidth() - margin,
        toTransform.getHeight() - margin), paint);
    canvas.drawRoundRect(new RectF(margin, margin, toTransform.getWidth() - margin,
        toTransform.getHeight() - margin), radius, radius, paint);

    if (toTransform != output) {
      toTransform.recycle();
    }

    return output;
  }

  @Override
  protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap inBitmap, int outWidth,
      int outHeight) {

    int width = inBitmap.getWidth();
    int height = inBitmap.getHeight();
    float right = width - margin;
    float bottom = height - margin;

    // Alpha is required for this transformation.
    Bitmap.Config safeConfig = getAlphaSafeConfig(inBitmap);
    Bitmap toTransform = getAlphaSafeBitmap(pool, inBitmap);
    Bitmap result = pool.get(toTransform.getWidth(), toTransform.getHeight(), safeConfig);

    result.setHasAlpha(true);

    BitmapShader shader =
        new BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setShader(shader);
    try {
      Canvas canvas = new Canvas(result);
      canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
      canvas.drawRoundRect(new RectF(margin, margin, right, margin + (radius * 2)), radius, radius,
          paint);
      canvas.drawRect(new RectF(margin, margin + radius, right, bottom), paint);
      clear(canvas);
    } finally {
    }

    if (!toTransform.equals(inBitmap)) {
      pool.put(toTransform);
    }

    return result;
  }

  @Override public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    messageDigest.update(ID_BYTES);
  }

  @NonNull private static Bitmap.Config getAlphaSafeConfig(@NonNull Bitmap inBitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Avoid short circuiting the sdk check.
      if (Bitmap.Config.RGBA_F16.equals(inBitmap.getConfig())) { // NOPMD
        return Bitmap.Config.RGBA_F16;
      }
    }

    return Bitmap.Config.ARGB_8888;
  }

  private static Bitmap getAlphaSafeBitmap(@NonNull BitmapPool pool,
      @NonNull Bitmap maybeAlphaSafe) {
    Bitmap.Config safeConfig = getAlphaSafeConfig(maybeAlphaSafe);
    if (safeConfig.equals(maybeAlphaSafe.getConfig())) {
      return maybeAlphaSafe;
    }

    Bitmap argbBitmap = pool.get(maybeAlphaSafe.getWidth(), maybeAlphaSafe.getHeight(), safeConfig);
    new Canvas(argbBitmap).drawBitmap(maybeAlphaSafe, 0 /*left*/, 0 /*top*/, null /*paint*/);

    // We now own this Bitmap. It's our responsibility to replace it in the pool outside this method
    // when we're finished with it.
    return argbBitmap;
  }

  // Avoids warnings in M+.
  private static void clear(Canvas canvas) {
    canvas.setBitmap(null);
  }
}