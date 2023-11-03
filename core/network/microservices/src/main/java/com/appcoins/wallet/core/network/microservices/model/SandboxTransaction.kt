package com.appcoins.wallet.core.network.microservices.model

data class SandboxTransaction(
  val uid: String?,
  val hash: String?,
  val status: TransactionStatus?,
  val validity: SandboxValidityState?,
  val errorCode: String? = null,
  val errorMessage: String? = null
) {

  enum class SandboxValidityState(val value: Int) {
    COMPLETED(0),
    PENDING(1),
    ERROR(2);

    companion object {
      fun toEnum(value: Int) = values().firstOrNull { it.value == value }
    }
  }


}
