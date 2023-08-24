package com.appcoins.wallet.core.network.eskills.install;

import rx.Completable;

/**
 * Created by trinkes on 9/8/16.
 */
public interface Installer {

  void dispatchInstallations();

  Completable install(String md5, boolean forceDefaultInstall, boolean shouldSetPackageInstaller);

  Completable update(String md5, boolean forceDefaultInstall, boolean shouldSetPackageInstaller);

  Completable downgrade(String md5, boolean forceDefaultInstall, boolean shouldSetPackageInstaller);

  Completable uninstall(String packageName);

  void stopDispatching();
}
