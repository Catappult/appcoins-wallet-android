package com.appcoins.wallet.billing.repository.entity

class TransactionData @JvmOverloads constructor(val type: String = UNKNOWN, val domain: String = UNKNOWN,
                                                val skuId: String, val payload: String = UNKNOWN) {

  companion object {
    const val UNKNOWN = "unknown"
  }

  enum class TransactionType {
    DONATION,
    INAPP;
  }
}
