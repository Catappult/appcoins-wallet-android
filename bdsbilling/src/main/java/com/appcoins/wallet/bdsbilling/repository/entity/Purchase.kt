package com.appcoins.wallet.bdsbilling.repository.entity

data class Purchase(val uid: String, val product: RemoteProduct, val status: String,
                    val autoRenewing: Boolean, val packageName: Package, val signature: Signature)

data class RemoteProduct(val name: String)

data class Package(val name: String)

data class Signature(val value: String, val message: String)