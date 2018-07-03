package com.asfoundation.wallet.widget;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import com.squareup.picasso.Transformation;

public class CardHeaderTransformation implements Transformation {
  private final int radius;
  int margin = 0;

  public CardHeaderTransformation(int radius) {
    this.radius = radius;
  }

  @Override public Bitmap transform(Bitmap source) {
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

    Bitmap output =
        Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    canvas.drawRect(
        new RectF(margin, margin + radius, source.getWidth() - margin, source.getHeight() - margin),
        paint);
    canvas.drawRoundRect(
        new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin), radius,
        radius, paint);

    if (source != output) {
      source.recycle();
    }

    return output;
  }

  @Override public String key() {
    return "card_header";
  }
}
