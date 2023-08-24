package com.appcoins.wallet.core.network.eskills.database;

import com.appcoins.wallet.core.network.eskills.install.InstallationPersistence;
import com.appcoins.wallet.core.network.eskills.room.InstallationDao;
import com.appcoins.wallet.core.network.eskills.room.RoomInstallation;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.BackpressureStrategy;
import java.util.List;
import rx.Completable;
import rx.Observable;
import rx.schedulers.Schedulers;

public class RoomInstallationPersistence implements InstallationPersistence {

  private InstallationDao installationDao;

  public RoomInstallationPersistence(InstallationDao installationDao) {
    this.installationDao = installationDao;
  }

  public Observable<List<RoomInstallation>> getInstallationsHistory() {
    return RxJavaInterop.toV1Observable(installationDao.getAll(), BackpressureStrategy.BUFFER)
        .subscribeOn(Schedulers.io());
  }

  public Completable insertAll(List<RoomInstallation> roomInstallationList) {
    return Completable.fromAction(() -> installationDao.insertAll(roomInstallationList))
        .subscribeOn(Schedulers.io());
  }

  public Completable insert(RoomInstallation roomInstallation) {
    return Completable.fromAction(() -> installationDao.insert(roomInstallation))
        .subscribeOn(Schedulers.io());
  }
}
