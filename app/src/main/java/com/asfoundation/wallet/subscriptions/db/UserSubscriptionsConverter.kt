package com.asfoundation.wallet.subscriptions.db

import androidx.room.TypeConverter
import com.appcoins.wallet.core.network.microservices.model.SubscriptionSubStatus
import java.math.BigDecimal

class UserSubscriptionsConverter {
  @TypeConverter
  fun fromBigDecimalString(value: String?): BigDecimal =
      if (value == null || value.isBlank()) BigDecimal.ZERO else BigDecimal(value)

  @TypeConverter
  fun toString(bigDecimal: BigDecimal?): String? = bigDecimal?.toPlainString()

  @TypeConverter
  fun fromSubStatusString(value: String): SubscriptionSubStatus {
    return SubscriptionSubStatus.valueOf(value)
  }

  @TypeConverter
  fun fromSubStatus(subStatus: SubscriptionSubStatus): String {
    return subStatus.name
  }
}
