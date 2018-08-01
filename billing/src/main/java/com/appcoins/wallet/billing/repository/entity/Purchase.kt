package com.appcoins.wallet.billing.repository.entity

data class Purchase(val uid: String, val product: RemoteProduct, val status: String, val packageName: Package,
                    val signature: Signature)

data class RemoteProduct(val name: String)

data class Package(val name: String)

data class Signature(val value: String, val message: Message)

data class Message(val orderId: String, val packageName: String, val productId: String,
                   val purchaseTime: Long, val purchaseToken: String,
                   val purchaseState: Integer, val developerPayload: String)
