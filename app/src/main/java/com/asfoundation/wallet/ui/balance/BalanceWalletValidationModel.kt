package com.asfoundation.wallet.ui.balance

data class BalanceWalletValidationModel(
    val address: String,
    val status: BalanceWalletValidationStatus
)

enum class BalanceWalletValidationStatus {
  VERIFIED, UNVERIFIED, CODE_REQUESTED, NO_NETWORK
}