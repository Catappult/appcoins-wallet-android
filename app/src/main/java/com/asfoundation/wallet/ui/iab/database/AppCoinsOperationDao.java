package com.asfoundation.wallet.ui.iab.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import io.reactivex.Flowable;
import java.util.List;

@Dao public interface AppCoinsOperationDao {
  @Query("select * from AppCoinsOperation") Flowable<List<AppCoinsOperation>> getAllAsFlowable();

  @Query("select * from AppCoinsOperation") List<AppCoinsOperation> getAll();

  @Query("select * from AppCoinsOperation where transaction_id like :key limit 1")
  Flowable<AppCoinsOperation> getAsFlowable(String key);

  @Query("select * from AppCoinsOperation where transaction_id like :key limit 1")
  AppCoinsOperation get(String key);

  @Insert void insert(AppCoinsOperation data);

  @Delete void delete(AppCoinsOperation data);
}
