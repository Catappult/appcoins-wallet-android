package com.appcoins.wallet.core.utils.jvm_common

import io.reactivex.Single

interface CountryCodeProvider {
  val countryCode: Single<String?>
}

fun convertCountryCode(countryCode: String): ByteArray {
  val data = ByteArray(2)
  val chars = countryCode.toCharArray()
  //mapDarkIcons country code for contract's format
  val index = (chars[0].toInt() - 65) * 26 + (chars[1].toInt() - 65)
  data[0] = (index ushr 8 and 0xFF).toByte()
  data[1] = (index and 0xFF).toByte()
  return data
}
