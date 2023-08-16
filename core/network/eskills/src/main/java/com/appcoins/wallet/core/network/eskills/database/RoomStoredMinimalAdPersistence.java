package com.appcoins.wallet.core.network.eskills.database;


import com.appcoins.wallet.core.network.eskills.room.RoomStoredMinimalAd;
import com.appcoins.wallet.core.network.eskills.room.StoredMinimalAdDAO;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.reactivex.BackpressureStrategy;
import io.reactivex.schedulers.Schedulers;
import rx.Observable;

public class RoomStoredMinimalAdPersistence implements StoredMinimalAdPersistence {

  private final StoredMinimalAdDAO storedMinimalAdDAO;

  public RoomStoredMinimalAdPersistence(StoredMinimalAdDAO storedMinimalAdDAO) {
    this.storedMinimalAdDAO = storedMinimalAdDAO;
  }

  @Override public Observable<RoomStoredMinimalAd> get(String packageName) {
    return RxJavaInterop.toV1Observable(storedMinimalAdDAO.get(packageName)
        .subscribeOn(Schedulers.io())
        .onErrorReturn(throwable -> null)
        .doOnError(Throwable::printStackTrace), BackpressureStrategy.BUFFER);
  }

  @Override public void remove(RoomStoredMinimalAd storedMinimalAd) {
    storedMinimalAdDAO.delete(storedMinimalAd);
  }

  @Override public void insert(RoomStoredMinimalAd storedMinimalAd) {
    storedMinimalAdDAO.insert(storedMinimalAd);
  }
}