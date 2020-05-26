package com.appcoins.wallet.bdsbilling.repository

enum class BillingSupportedType {
  INAPP, INAPP_UNMANAGED, SUBS, SUBS_UNMANAGED, DONATION;


  companion object {

    @JvmStatic
    fun valueOfInsensitive(value: String): BillingSupportedType {
      return values().firstOrNull { it.name.equals(value, true) }
          ?: throw IllegalArgumentException(Throwable("$value is not supported"))
    }

    @JvmStatic
    fun valueOfManagedType(value: String): BillingSupportedType {
      return mapToManagedType(valueOfInsensitive(value))
    }

    @JvmStatic
    fun valueOfProductType(value: String): BillingSupportedType {
      val type = valueOfInsensitive(value)
      if (type == INAPP || type == SUBS) {
        return type
      } else {
        throw IllegalArgumentException(Throwable("$value is not a product type supported"))
      }
    }

    private fun mapToManagedType(type: BillingSupportedType): BillingSupportedType {
      return when (type) {
        INAPP_UNMANAGED -> INAPP
        DONATION -> INAPP
        SUBS_UNMANAGED -> SUBS
        INAPP -> INAPP
        SUBS -> SUBS
      }
    }
  }

}