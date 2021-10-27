package com.asfoundation.wallet.logging.send_logs

import androidx.room.*
import io.reactivex.Completable

@Dao
interface LogsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertLog(log: LogEntity): Completable

  @Query("""
    DELETE FROM LogEntity WHERE id IN (
        SELECT id FROM LogEntity 
        WHERE NOT sending 
        AND (SELECT COUNT(id) FROM LogEntity WHERE NOT sending) > :max_logs
        ORDER BY created ASC 
        LIMIT 1)
    """)
  fun removeOldLogs(max_logs: Int = 10)

  @Transaction
  fun saveLog(log: LogEntity) {
    insertLog(log)
    removeOldLogs()
  }
}