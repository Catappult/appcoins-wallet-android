package com.appcoins.wallet.billing.repository.entity


class Gateway(val name: Name, val label: String, val icon: String) {

  companion object {
    fun unknown(): Gateway {
      return Gateway(Name.unknown, "unknown", "unknown")
    }
  }

  enum class Name {
    appcoins, adyen, unknown
  }
}