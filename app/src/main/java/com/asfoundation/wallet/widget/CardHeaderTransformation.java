package com.asfoundation.wallet.widget;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
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
  protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth,
      int outHeight) {

    int width = toTransform.getWidth();
    int height = toTransform.getHeight();

    Bitmap bitmap = pool.get(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setHasAlpha(true);

    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setShader(new BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

    float right = width - margin;
    float bottom = height - margin;

    canvas.drawRoundRect(new RectF(margin, margin, right, margin + (radius * 2)), radius, radius,
        paint);
    canvas.drawRect(new RectF(margin, margin + radius, right, bottom), paint);

    return bitmap;
  }

  @Override public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    messageDigest.update(ID_BYTES);
  }
}