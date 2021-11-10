package com.asfoundation.wallet.logging.send_logs

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Manages LogEntities. These Logs can be uploaded to an aws bucket
 * using the 'Sent Logs to Support Tem' feature.
 */
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
  fun removeOldestLog(max_logs: Int = 10)

  /**
   * Inserts a logEntity in the database and deletes the oldest log
   * if the are more than 10 logs that aren't in the process of being sent.
   */
  @Transaction
  fun saveLog(log: LogEntity) {
    insertLog(log)
    removeOldestLog()
  }

  @Query("""
    SELECT * FROM LogEntity 
    WHERE sending
  """)
  fun getSendingLogs(): Single<List<LogEntity>>

  /**
   * If there are logs already in the process of being sent
   * (i.e sending == true), does nothing.
   * Otherwise, sets all logs to sending.
   */
  @Query("""
    UPDATE LogEntity
    SET sending = 1
    WHERE NOT EXISTS (SELECT * FROM LogEntity WHERE sending)
  """)
  fun setLogsToSent(): Completable

  @Query("""
      DELETE FROM LogEntity
      WHERE sending
    """)
  fun deleteSentLogs(): Completable
}