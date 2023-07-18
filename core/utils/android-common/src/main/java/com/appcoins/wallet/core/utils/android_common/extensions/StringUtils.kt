package com.appcoins.wallet.core.utils.android_common.extensions

object StringUtils {
  fun String.masked() = replaceRange(IntRange(6, length - 5), " ··· ")
  fun String.maskedEnd() = replaceRange(length - 20 until length, "...")

}