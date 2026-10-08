package com.asfoundation.wallet.ui.iab.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface AppCoinsOperationDao {
  @Query("select * from AppCoinsOperationEntity")
  fun getAllAsFlowable(): Flowable<List<AppCoinsOperationEntity>>

  @Query("select * from AppCoinsOperationEntity")
  fun getAll(): List<AppCoinsOperationEntity>

  @Query("select * from AppCoinsOperationEntity where `key` like :key limit 1")
  fun getAsFlowable(key: String): Flowable<AppCoinsOperationEntity>

  @Query("select * from AppCoinsOperationEntity where `key` like :key limit 1")
  fun get(key: String): AppCoinsOperationEntity

  @Insert
  fun insert(data: AppCoinsOperationEntity)

  @Delete
  fun delete(data: AppCoinsOperationEntity)
}
