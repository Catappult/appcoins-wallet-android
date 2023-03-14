package com.appcoins.wallet.bdsbilling.repository.entity

import java.util.*

data class Purchase(val uid: String, val product: RemoteProduct, val state: State,
                    val autoRenewing: Boolean, val renewal: Date?, val packageName: Package,
                    val signature: Signature)

data class RemoteProduct(val name: String)

data class Package(val name: String)

data class Signature(val value: String, val message: String)

enum class State {
  CONSUMED, PENDING, ACKNOWLEDGED
}