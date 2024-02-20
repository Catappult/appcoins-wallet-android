package com.asfoundation.wallet.permissions.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface PermissionsDao {
  @Query("select * from PermissionEntity where `key` like :key")
  fun getSyncPermission(key: String): PermissionEntity?

  @Query("select * from PermissionEntity where `key` like :key")
  fun getPermission(key: String): Flowable<PermissionEntity>

  @Query("select * from PermissionEntity order by `key`")
  fun getAllAsFlowable(): Flowable<List<PermissionEntity>>

  @Query("select * from PermissionEntity order by `key`") fun getAll(): List<PermissionEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insert(roomPermission: PermissionEntity)

  @Delete fun remove(roomPermission: PermissionEntity?)
}
