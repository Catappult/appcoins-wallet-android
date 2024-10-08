package com.asfoundation.wallet.onboarding_new_payment.utils

class OnboardingUtils {
  companion object {
    fun isSdkVersionAtLeast2(sdkVersion: String?): Boolean {
      return (sdkVersion?.toIntOrNull() ?: -1) >= 2
    }
  }
}