package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class AppInfoProvider {
  private final Context context;
  private final ImageSaver imageSaver;

  public AppInfoProvider(Context context, ImageSaver imageSaver) {
    this.context = context;
    this.imageSaver = imageSaver;
  }

  /**
   * This function gets information about the app with the given packageName.
   * Since the info is collected from the android framework, it is slow. Avoid calling this
   * method from main thread
   */
  public AppCoinsOperation get(String id, String packageName, String productName)
      throws UnknownApplicationException, ImageSaver.SaveException {
    try {
      PackageManager packageManager = context.getPackageManager();
      ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
      Drawable icon = app.loadIcon(packageManager);
      String path = imageSaver.save(packageName, icon);
      String applicationName = packageManager.getApplicationLabel(app)
          .toString();
      return new AppCoinsOperation(id, packageName, applicationName, path, productName);
    } catch (PackageManager.NameNotFoundException e) {
      throw new UnknownApplicationException(
          "Unable to find the application with the packageName: " + packageName);
    }
  }

  public class UnknownApplicationException extends Throwable {
    private UnknownApplicationException(String message) {
      super(message);
    }
  }
}