package com.appcoins.wallet.ui.widgets;

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
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import java.security.MessageDigest;

public class CardHeaderTransformation extends BitmapTransformation {
  private static final byte[] ID_BYTES = "card_header".getBytes(Key.CHARSET);
  private final int radius;
  int margin = 0;

  public CardHeaderTransformation(int radius) {
    this.radius = radius;
  }

  @NonNull private static Bitmap.Config getAlphaSafeConfig(@NonNull Bitmap inBitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (Bitmap.Config.RGBA_F16.equals(inBitmap.getConfig())) {
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
    new Canvas(argbBitmap).drawBitmap(maybeAlphaSafe, 0, 0, null);
    return argbBitmap;
  }

  private static void clear(Canvas canvas) {
    canvas.setBitmap(null);
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
    Canvas canvas = new Canvas(result);
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    canvas.drawRoundRect(new RectF(margin, margin, right, margin + (radius * 2)), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin, margin + radius, right, bottom), paint);
    clear(canvas);

    if (!toTransform.equals(inBitmap)) {
      pool.put(toTransform);
    }

    return result;
  }

  @Override public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    messageDigest.update(ID_BYTES);
  }
}