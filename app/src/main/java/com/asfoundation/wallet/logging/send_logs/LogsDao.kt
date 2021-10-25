package com.asfoundation.wallet.logging.send_logs

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface LogsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertLog(log: LogEntity): Completable

  @Transaction
  fun saveLog(log: LogEntity) {
    insertLog(log)
    removeOldLogs()
  }

  @Query("""
    WITH del AS (
    SELECT TOP 1 FROM LogEntity 
    WHERE sending = 'False' 
    AND (COUNT * FROM LogEntity WHERE WHERE sending = 'False') > :max_logs
    ORDER BY created DESC)
    DELETE FROM del
  """)
  fun removeOldLogs()
}