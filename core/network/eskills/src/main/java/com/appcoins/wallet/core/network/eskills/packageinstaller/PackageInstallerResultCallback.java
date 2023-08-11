package com.appcoins.wallet.core.network.eskills.packageinstaller;

import android.os.Bundle;

public interface PackageInstallerResultCallback {

  void onInstallationResult(InstallStatus installStatus);

  void onPendingUserAction(Bundle extras);
}
