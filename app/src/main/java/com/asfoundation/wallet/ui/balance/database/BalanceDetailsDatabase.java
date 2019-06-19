package com.asfoundation.wallet.ui.balance.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = BalanceDetailsEntity.class, version = 1)
public abstract class BalanceDetailsDatabase extends RoomDatabase {
  public abstract BalanceDetailsDao balanceDetailsDao();
}