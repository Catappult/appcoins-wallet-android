package com.appcoins.wallet.gamification.repository

import androidx.room.TypeConverter
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import java.math.BigDecimal

class PromotionTypeConverter {

  @TypeConverter
  fun fromString(value: String?): BigDecimal =
      if (value == null || value.isBlank()) BigDecimal.ZERO else BigDecimal(value)

  @TypeConverter
  fun toString(bigDecimal: BigDecimal?): String? = bigDecimal?.toPlainString()

  @TypeConverter
  fun convertFromPromotionStatus(type: String?): PromotionsResponse.Status? {
    return type?.let { PromotionsResponse.Status.valueOf(it) }
  }

  @TypeConverter
  fun convertPromotionStatus(status: PromotionsResponse.Status?): String? {
    return status?.name
  }

  @TypeConverter
  fun convertFromUserStatus(type: String?): ReferralResponse.UserStatus? {
    return type?.let { ReferralResponse.UserStatus.valueOf(it) }
  }

  @TypeConverter
  fun convertUserStatus(status: ReferralResponse.UserStatus?): String? {
    return status?.name
  }

}