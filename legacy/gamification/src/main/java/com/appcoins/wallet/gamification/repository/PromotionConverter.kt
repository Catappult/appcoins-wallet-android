package com.appcoins.wallet.gamification.repository

import androidx.room.TypeConverter
import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.PromotionsResponse
import com.appcoins.wallet.gamification.repository.entity.ReferralResponse
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import java.math.BigDecimal
import java.util.*

class PromotionConverter {

  @TypeConverter
  fun fromString(value: String?): BigDecimal? =
      if (value == null || value.isBlank()) null else BigDecimal(value)

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

  @TypeConverter
  fun convertFromLevelsStatus(type: String?): LevelsResponse.Status? {
    return type?.let { LevelsResponse.Status.valueOf(it) }
  }

  @TypeConverter
  fun convertLevelsStatus(status: LevelsResponse.Status?): String? {
    return status?.name
  }

  @TypeConverter
  fun convertFromDate(date: Long?): Date? {
    return date?.let { Date(it) }
  }

  @TypeConverter
  fun convertDate(date: Date): Long? {
    return date.time
  }

  @TypeConverter
  fun convertFromWalletOrigin(walletOrigin: String): WalletOrigin {
    return WalletOrigin.valueOf(walletOrigin)
  }

  @TypeConverter
  fun convertWalletOrigin(walletOrigin: WalletOrigin): String {
    return walletOrigin.name
  }
}