package com.asfoundation.wallet.permissions.repository

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface PermissionsDao {
  @Query("select * from PermissionEntity where `key` like :key")
  fun getSyncPermission(key: String): PermissionEntity?

  @Query("select * from PermissionEntity where `key` like :key")
  fun getPermission(key: String): Flowable<PermissionEntity>

  @Query("select * from PermissionEntity")
  fun getAllAsFlowable(): Flowable<List<PermissionEntity>>

  @Query("select * from PermissionEntity")
  fun getAll(): List<PermissionEntity>

  @Insert
  fun insert(roomPermission: PermissionEntity)

  @Delete
  fun remove(roomPermission: PermissionEntity?)

}
