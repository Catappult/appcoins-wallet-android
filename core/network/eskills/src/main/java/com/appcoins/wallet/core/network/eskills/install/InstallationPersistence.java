package com.appcoins.wallet.core.network.eskills.install;


import com.appcoins.wallet.core.network.eskills.room.RoomInstallation;
import java.util.List;
import rx.Completable;
import rx.Observable;

public interface InstallationPersistence {
  Observable<List<RoomInstallation>> getInstallationsHistory();

  Completable insertAll(List<RoomInstallation> roomInstallationList);

  Completable insert(RoomInstallation roomInstallation);
}
