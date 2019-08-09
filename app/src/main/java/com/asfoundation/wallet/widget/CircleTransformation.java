package com.asfoundation.wallet.widget;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.squareup.picasso.Transformation;

/**
 * Created by Joao Raimundo on 19/05/2018.
 */
public class CircleTransformation implements Transformation {
  @Override public Bitmap transform(Bitmap source) {
    int size = Math.min(source.getWidth(), source.getHeight());

    int x = (source.getWidth() - size) / 2;
    int y = (source.getHeight() - size) / 2;

    Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
    if (squaredBitmap != source) {
      source.recycle();
    }

    Bitmap bitmap = Bitmap.createBitmap(size, size, squaredBitmap.getConfig());

    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    BitmapShader shader =
        new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
    paint.setShader(shader);
    paint.setAntiAlias(true);

    float r = size / 2f;
    float radius = size / 2.5f;
    canvas.drawCircle(r, r, radius, paint);

    squaredBitmap.recycle();
    return bitmap;
  }

  @Override public String key() {
    return "circle";
  }
}
