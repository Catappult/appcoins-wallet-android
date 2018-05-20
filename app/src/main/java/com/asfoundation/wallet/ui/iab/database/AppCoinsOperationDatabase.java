package com.asfoundation.wallet.ui.iab.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = AppCoinsOperation.class, version = 1)
public abstract class AppCoinsOperationDatabase extends RoomDatabase {
  public abstract AppCoinsOperationDao appCoinsOperationDao();
}
