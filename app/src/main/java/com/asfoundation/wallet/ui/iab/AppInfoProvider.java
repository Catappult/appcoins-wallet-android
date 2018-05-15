package com.asfoundation.wallet.ui.iab;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import org.jetbrains.annotations.Nullable;

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
   *
   * @return Information about the app with the packageName given as argument or null if there
   * is no app with the given packageName
   */
  public @Nullable InAppPurchaseData get(String id, String packageName, String productName) {
    try {
      PackageManager packageManager = context.getPackageManager();
      ApplicationInfo app = packageManager.getApplicationInfo(packageName, 0);
      Drawable icon = app.loadIcon(packageManager);
      String path = imageSaver.save(icon);
      String applicationName = packageManager.getApplicationLabel(app)
          .toString();
      return new InAppPurchaseData(id, packageName, applicationName, path, productName);
    } catch (PackageManager.NameNotFoundException e) {
      return null;
    }
  }
}