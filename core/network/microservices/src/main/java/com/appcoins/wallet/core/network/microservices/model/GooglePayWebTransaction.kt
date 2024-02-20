package com.appcoins.wallet.core.network.microservices.model

data class GooglePayWebTransaction(
    val uid: String?,
    val hash: String?,
    val status: TransactionStatus?,
    val validity: GooglePayWebValidityState?,
    val sessionId: String?,
    val sessionData: String?,
    val errorCode: String? = null,
    val errorMessage: String? = null
) {

  enum class GooglePayWebValidityState(val value: Int) {
    COMPLETED(0),
    PENDING(1),
    ERROR(2);

    companion object {
      fun toEnum(value: Int) = values().firstOrNull { it.value == value }
    }
  }
}
