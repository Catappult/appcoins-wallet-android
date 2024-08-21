package com.asfoundation.wallet.ui.iab;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageSaver {
  private final String cacheFolderPath;

  public ImageSaver(String cacheFolderPath) {
    this.cacheFolderPath = cacheFolderPath;
    new File(cacheFolderPath).mkdirs();
  }

  public String save(String fileName, Drawable icon) throws SaveException {
    Bitmap bitmap = drawableToBitmap(icon);
    String filePath = cacheFolderPath + fileName;
    if (saveToFile(bitmap, filePath)) {
      return filePath;
    }
    throw new SaveException(filePath);
  }

  private boolean saveToFile(Bitmap bitmap, String file) {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(file);
      return bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
      // PNG is a lossless format, the compression factor (100) is ignored
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  public Bitmap drawableToBitmap(Drawable drawable) {
    Bitmap bitmap = null;

    if (drawable instanceof BitmapDrawable bitmapDrawable) {
      if (bitmapDrawable.getBitmap() != null) {
        return bitmapDrawable.getBitmap();
      }
    }

    if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
      bitmap = Bitmap.createBitmap(1, 1,
          Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
    } else {
      bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
          Bitmap.Config.ARGB_8888);
    }

    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

  public class SaveException extends Throwable {
    public SaveException(String filePath) {
      super("Unable to write file in " + filePath);
    }
  }
}
