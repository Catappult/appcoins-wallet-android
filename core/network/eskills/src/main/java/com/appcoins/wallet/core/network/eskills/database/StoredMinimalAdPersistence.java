package com.appcoins.wallet.core.network.eskills.database;

import com.appcoins.wallet.core.network.eskills.room.RoomStoredMinimalAd;
import rx.Observable;

public interface StoredMinimalAdPersistence {

  Observable<RoomStoredMinimalAd> get(String packageName);

  void remove(RoomStoredMinimalAd storedMinimalAd);

  void insert(RoomStoredMinimalAd storedMinimalAd);
}
