package com.appcoins.wallet.feature.walletInfo.data.wallet.db

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.math.BigInteger

class WalletInfoTypeConverter {
  @TypeConverter
  fun fromBigIntegerString(value: String?): BigInteger =
      if (value == null || value.isBlank()) BigInteger.ZERO else BigInteger(value)

  @TypeConverter
  fun toString(bigInteger: BigInteger?): String? = bigInteger?.toString()

  @TypeConverter
  fun fromBigDecimalString(value: String?): BigDecimal =
      if (value == null || value.isBlank()) BigDecimal.ZERO else BigDecimal(value)

  @TypeConverter
  fun toString(bigDecimal: BigDecimal?): String? = bigDecimal?.toPlainString()
}