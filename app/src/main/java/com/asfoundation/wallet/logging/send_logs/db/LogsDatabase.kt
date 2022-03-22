package com.asfoundation.wallet.logging.send_logs.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.asfoundation.wallet.logging.send_logs.InstantTypeConverter

@Database(entities = [LogEntity::class], version = 1)
@TypeConverters(InstantTypeConverter::class)
abstract class LogsDatabase : RoomDatabase() {
  abstract fun logsDao(): LogsDao
}