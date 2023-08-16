package com.appcoins.wallet.core.network.eskills.install;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import cm.aptoide.pt.app.aptoideinstall.AptoideInstallManager;

import com.appcoins.wallet.core.network.eskills.database.RoomStoredMinimalAdPersistence;
import com.appcoins.wallet.core.network.eskills.room.RoomInstalled;
import com.appcoins.wallet.core.network.eskills.utils.logger.Logger;

import com.appcoins.wallet.core.network.eskills.utils.utils.AptoideUtils;
import com.appcoins.wallet.core.network.eskills.utils.utils.FileUtils;
import javax.inject.Inject;
import rx.Completable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class InstalledIntentService extends IntentService {

  private static final String TAG = InstalledIntentService.class.getName();

  @Inject RoomStoredMinimalAdPersistence roomStoredMinimalAdPersistence;

  @Inject AptoideInstallManager aptoideInstallManager;
  @Inject FileUtils fileUtils;
  private SharedPreferences sharedPreferences;
  private CompositeSubscription subscriptions;
  private InstallManager installManager;


  private PackageManager packageManager;

  public InstalledIntentService() {
    super("InstalledIntentService");
  }

  @Override protected void onHandleIntent(Intent intent) {
    if (intent != null) {
      final String action = intent.getAction();
      final String packageName = intent.getData()
          .getEncodedSchemeSpecificPart();

      if (!TextUtils.equals(action, Intent.ACTION_PACKAGE_REPLACED) && intent.getBooleanExtra(
          Intent.EXTRA_REPLACING, false)) {
        // do nothing if its a replacement ongoing. we are only interested in
        // already replaced apps
        return;
      }

      switch (action) {
        case Intent.ACTION_PACKAGE_ADDED:
          onPackageAdded(packageName);
          break;
        case Intent.ACTION_PACKAGE_REPLACED:
          onPackageReplaced(packageName);
          break;
        case Intent.ACTION_PACKAGE_REMOVED:
          onPackageRemoved(packageName);
          break;
      }
    }
  }

  protected void onPackageAdded(String packageName) {
    Logger.getInstance()
        .d(TAG, "Package added: " + packageName);

    PackageInfo packageInfo = databaseOnPackageAdded(packageName);

    aptoideInstallManager.persistCandidate(packageName);
  }

  protected void onPackageReplaced(String packageName) {
    Logger.getInstance()
        .d(TAG, "Packaged replaced: " + packageName);
    PackageInfo packageInfo = databaseOnPackageReplaced(packageName);


    aptoideInstallManager.persistCandidate(packageName);
  }

  protected void onPackageRemoved(String packageName) {
    Logger.getInstance()
        .d(TAG, "Packaged removed: " + packageName);
    sendUninstallEvent(packageName);

  }

  private PackageInfo databaseOnPackageAdded(String packageName) {
    PackageInfo packageInfo = AptoideUtils.SystemU.getPackageInfo(packageName, getPackageManager());

    if (checkAndLogNullPackageInfo(packageInfo, packageName)) {
      return packageInfo;
    }
    RoomInstalled installed = new RoomInstalled(packageInfo, packageManager, fileUtils);
    installManager.onAppInstalled(installed)
        .subscribe(() -> {
        });
    return packageInfo;
  }




  private void sendUninstallEvent(String packageName) {

  }



  private PackageInfo databaseOnPackageReplaced(String packageName) {

    PackageInfo packageInfo = AptoideUtils.SystemU.getPackageInfo(packageName, getPackageManager());

    if (checkAndLogNullPackageInfo(packageInfo, packageName)) {
      return packageInfo;
    }
    return packageInfo;
  }



  /**
   * @param packageInfo packageInfo.
   *
   * @return true if packageInfo is null, false otherwise.
   */
  private boolean checkAndLogNullPackageInfo(PackageInfo packageInfo, String packageName) {
    if (packageInfo == null) {
      return true;
    } else {
      return false;
    }
  }




}
