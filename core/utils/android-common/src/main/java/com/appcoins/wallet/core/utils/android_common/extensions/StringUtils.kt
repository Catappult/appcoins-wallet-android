package com.appcoins.wallet.core.utils.android_common.extensions

object StringUtils {
  fun String.masked(nStartChars: Int = 6, nEndChars: Int = 5) = replaceRange(IntRange(nStartChars, length - nEndChars), "...")
  fun String.maskedEnd() = replaceRange(length - 20 until length, "...")

  fun String.simpleFormat(): String {
    return if (length <= 17) {
      this
    } else {
      substring(0, 17) + "..."
    }
  }
}
