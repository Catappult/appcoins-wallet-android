package com.asfoundation.wallet.ui.iab.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = InAppPurchaseData.class, version = 1)
public abstract class InAppPurchaseDatabase extends RoomDatabase {
  public abstract InAppPurchaseDataDao inAppPurchaseDataDao();
}
