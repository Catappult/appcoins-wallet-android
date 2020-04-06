package com.appcoins.wallet.bdsbilling.repository

enum class BillingSupportedType {
  INAPP, SUBS;


  companion object {
    @JvmStatic
    fun valueOfInsensitive(value: String): BillingSupportedType {
      return values().firstOrNull { it.name.equals(value, true) }
          ?: INAPP
    }
  }

}