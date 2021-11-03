package com.appcoins.wallet.bdsbilling.repository

enum class BillingSupportedType {
  INAPP, INAPP_UNMANAGED, INAPP_SUBSCRIPTION, SUBS_UNMANAGED, DONATION, ESKILLS;


  companion object {

    const val SUBS_TYPE = "subs"

    @JvmStatic
    fun valueOfInsensitive(value: String): BillingSupportedType {
      return values().firstOrNull { it.name.equals(value, true) }
          ?: throw IllegalArgumentException(Throwable("$value is not supported"))
    }

    @JvmStatic
    fun valueOfManagedType(value: String): BillingSupportedType {
      return mapToProductType(valueOfInsensitive(value))
    }

    @JvmStatic
    fun valueOfProductType(value: String): BillingSupportedType {
      val type = valueOfInsensitive(value)
      if (type == INAPP || type == INAPP_SUBSCRIPTION) {
        return type
      } else {
        throw IllegalArgumentException(Throwable("$value is not a product type supported"))
      }
    }

    //Use this method on methods that communicate with SDK
    @JvmStatic
    fun valueOfItemType(value: String): BillingSupportedType {
      return when {
        value.equals(INAPP.name, true) -> INAPP
        value.equals(SUBS_TYPE, true) -> INAPP_SUBSCRIPTION
        else -> {
          throw IllegalArgumentException(Throwable("$value is not a product type supported"))
        }
      }
    }

    @JvmStatic
    fun isManagedType(type: BillingSupportedType): Boolean {
      return type == INAPP || type == INAPP_SUBSCRIPTION
    }

    fun mapToProductType(type: BillingSupportedType): BillingSupportedType {
      return when (type) {
        INAPP_UNMANAGED -> INAPP
        DONATION -> INAPP
        INAPP -> INAPP
        SUBS_UNMANAGED -> INAPP_SUBSCRIPTION
        INAPP_SUBSCRIPTION -> INAPP_SUBSCRIPTION
        ESKILLS -> ESKILLS
      }
    }
  }
}