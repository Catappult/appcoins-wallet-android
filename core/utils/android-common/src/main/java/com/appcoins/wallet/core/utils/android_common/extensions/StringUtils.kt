package com.appcoins.wallet.core.utils.android_common.extensions

object StringUtils {
  fun String.masked() = "${substring(0, 6)}...${substring(lastIndex - 4, lastIndex)}"
}