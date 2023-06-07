package com.appcoins.wallet.feature.walletInfo.data.verification

data class BalanceVerificationModel(val address: String,
                                    val cachedStatus: BalanceVerificationStatus,
                                    val status: BalanceVerificationStatus?)

enum class BalanceVerificationStatus {
  VERIFIED, UNVERIFIED, CODE_REQUESTED, NO_NETWORK, ERROR, VERIFYING
}