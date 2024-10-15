package com.appcoins.wallet.feature.walletInfo.data.verification

data class VerificationStatusCompound(
  val creditCardStatus: VerificationStatus,
  val payPalStatus: VerificationStatus,
  val currentVerificationType: VerificationType?
)
