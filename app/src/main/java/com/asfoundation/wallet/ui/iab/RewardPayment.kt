package com.asfoundation.wallet.ui.iab

data class RewardPayment(val txId: String?,
                         val orderReference: String?,
                         val status: Status, val errorCode: Int? = null,
                         val errorMessage: String? = null)

enum class Status {
  PROCESSING, COMPLETED, ERROR, FORBIDDEN, NO_NETWORK
}