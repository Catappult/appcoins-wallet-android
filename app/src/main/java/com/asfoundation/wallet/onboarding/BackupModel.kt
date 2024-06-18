package com.asfoundation.wallet.onboarding

data class BackupModel(val backupPrivateKey: String, val flow: String)

enum class VerificationFlow {
  VERIFY_PAYPAL,
  VERIFY_CREDIT_CARD
}