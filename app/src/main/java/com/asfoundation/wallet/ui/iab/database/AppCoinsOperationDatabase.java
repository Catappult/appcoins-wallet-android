package com.asfoundation.wallet.ui.iab.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = AppCoinsOperationEntity.class, version = 1)
public abstract class AppCoinsOperationDatabase extends RoomDatabase {
  public abstract AppCoinsOperationDao appCoinsOperationDao();
}
