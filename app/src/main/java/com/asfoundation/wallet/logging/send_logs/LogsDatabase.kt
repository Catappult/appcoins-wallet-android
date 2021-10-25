package com.asfoundation.wallet.logging.send_logs

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [LogEntity::class], version = 2)
@TypeConverters(InstantTypeConverter::class)
abstract class LogsDatabase : RoomDatabase() {
  abstract fun logsDao(): LogsDao
}