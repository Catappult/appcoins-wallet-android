package com.asfoundation.wallet.logging.send_logs

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface LogsDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertLog(log: LogEntity)

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

  @Query("""
    SELECT * FROM LogEntity 
    WHERE sending
  """)
  fun getSendingLogs(): Single<List<LogEntity>>

  @Query("""
    UPDATE LogEntity
    SET sending = 1
    WHERE NOT EXISTS (SELECT * FROM LogEntity WHERE sending)
  """)
  fun updateLogs(): Completable

  @Query("""
      DELETE FROM LogEntity
      WHERE sending
    """)
  fun deleteSentLogs(): Completable
}