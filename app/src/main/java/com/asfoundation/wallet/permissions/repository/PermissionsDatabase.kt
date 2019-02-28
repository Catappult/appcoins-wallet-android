package com.asfoundation.wallet.permissions.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PermissionEntity::class], version = 1)
@TypeConverters(PermissionsListTypeConverter::class)
abstract class PermissionsDatabase : RoomDatabase() {
  abstract fun permissionsDao(): PermissionsDao
}