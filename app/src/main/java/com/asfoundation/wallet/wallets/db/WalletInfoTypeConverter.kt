package com.asfoundation.wallet.wallets.db

import androidx.room.TypeConverter
import java.math.BigInteger

class WalletInfoTypeConverter {
  @TypeConverter
  fun fromBigIntegerString(value: String?): BigInteger =
      if (value == null || value.isBlank()) BigInteger.ZERO else BigInteger(value)

  @TypeConverter
  fun toString(bigInteger: BigInteger?): String? = bigInteger?.toString()
}