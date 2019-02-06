package com.asfoundation.wallet.permissions.repository

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(entities = [PermissionEntity::class], version = 1)
@TypeConverters(PermissionsListTypeConverter::class)
abstract class PermissionsDatabase : RoomDatabase() {
  abstract fun permissionsDao(): PermissionsDao
}