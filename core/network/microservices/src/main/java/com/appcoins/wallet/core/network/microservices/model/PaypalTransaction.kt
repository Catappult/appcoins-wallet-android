package com.appcoins.wallet.core.network.microservices.model

data class PaypalTransaction(
  val uid: String?,
  val hash: String?,
  val status: TransactionStatus?,
  val validity: PaypalValidityState?,
) {

  enum class PaypalValidityState(val value: Int) {
    COMPLETED(0),
    PENDING(1),
    NO_BILLING_AGREEMENT(2),  // Billing Agreement needed
    ERROR(3);

    companion object {
      fun toEnum(value: Int) = values().firstOrNull { it.value == value }
    }
  }


}
