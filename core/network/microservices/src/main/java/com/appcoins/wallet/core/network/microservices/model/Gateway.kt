package com.appcoins.wallet.core.network.microservices.model


class Gateway(val name: Name?, val label: String, val icon: String) {

  companion object {
    fun unknown(): Gateway {
      return Gateway(Name.unknown, "unknown", "unknown")
    }
  }

  enum class Name {
    appcoins, adyen_v2, unknown, appcoins_credits, myappcoins, challenge_reward
  }
}