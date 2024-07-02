package com.appcoins.wallet.core.network.microservices.model

data class TrueLayerTransaction(
  val uid: String?,
  val hash: String?,
  val status: TransactionStatus?,
  val validity: TrueLayerValidityState?,
  val paymentId: String?,
  val resourceToken: String?,
  val errorCode: String? = null,
  val errorMessage: String? = null
) {

  enum class TrueLayerValidityState(val value: Int) {
    COMPLETED(0),
    PENDING(1),
    ERROR(2);

    companion object {
      fun toEnum(value: Int) = values().firstOrNull { it.value == value }
    }
  }


}
