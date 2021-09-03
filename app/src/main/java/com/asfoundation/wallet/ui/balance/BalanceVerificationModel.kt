package com.asfoundation.wallet.ui.balance

data class BalanceVerificationModel(val address: String, val status: BalanceVerificationStatus)

enum class BalanceVerificationStatus {
  VERIFIED, UNVERIFIED, CODE_REQUESTED, NO_NETWORK, ERROR
}