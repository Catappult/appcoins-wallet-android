package com.appcoins.wallet.core.network.eskills.install;

import com.appcoins.wallet.core.network.eskills.packageinstaller.InstallStatus;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class AppInstallerStatusReceiver {

  private PublishSubject<InstallStatus> installStatusPublishSubject;

  public AppInstallerStatusReceiver(PublishSubject<InstallStatus> installStatusPublishSubject) {
    this.installStatusPublishSubject = installStatusPublishSubject;
  }

  public void onStatusReceived(InstallStatus installStatus) {
    installStatusPublishSubject.onNext(installStatus);
  }

  public Observable<InstallStatus> getInstallerInstallStatus() {
    return installStatusPublishSubject;
  }
}
