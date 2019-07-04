package com.asfoundation.wallet.util

fun String.isNumeric(): Boolean {
  return when (this.toIntOrNull()) {
    null -> false
    else -> true
  }
}

fun String.isNotNumeric(): Boolean {
  return !isNumeric()
}