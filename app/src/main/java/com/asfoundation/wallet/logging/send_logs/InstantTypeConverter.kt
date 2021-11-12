package com.asfoundation.wallet.logging.send_logs

import androidx.room.TypeConverter
import java.time.Instant


class InstantTypeConverter {
  @TypeConverter
  fun fromEpochMilli(epochMilli: Long?): Instant? {
    return epochMilli?.let { Instant.ofEpochMilli(epochMilli) }
  }

  @TypeConverter
  fun instantToEpochMilli(instant: Instant?): Long? {
    return instant?.toEpochMilli()
  }
}