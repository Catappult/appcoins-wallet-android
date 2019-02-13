package com.asfoundation.wallet.ui.iab.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Flowable;
import java.util.List;

@Dao public interface AppCoinsOperationDao {
  @Query("select * from AppCoinsOperationEntity")
  Flowable<List<AppCoinsOperationEntity>> getAllAsFlowable();

  @Query("select * from AppCoinsOperationEntity") List<AppCoinsOperationEntity> getAll();

  @Query("select * from AppCoinsOperationEntity where `key` like :key limit 1")
  Flowable<AppCoinsOperationEntity> getAsFlowable(String key);

  @Query("select * from AppCoinsOperationEntity where `key` like :key limit 1")
  AppCoinsOperationEntity get(String key);

  @Insert void insert(AppCoinsOperationEntity data);

  @Delete void delete(AppCoinsOperationEntity data);
}
