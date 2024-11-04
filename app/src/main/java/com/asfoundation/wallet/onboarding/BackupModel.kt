package com.asfoundation.wallet.onboarding

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BackupModel(
  val backupPrivateKey: String = "",
  val flow: String = "",
  val paymentFunnel: String? = null
) : Parcelable

enum class OnboardingFlow {
  VERIFY_PAYPAL,
  VERIFY_CREDIT_CARD,
  ONBOARDING_PAYMENT,
  ONBOARDING
}