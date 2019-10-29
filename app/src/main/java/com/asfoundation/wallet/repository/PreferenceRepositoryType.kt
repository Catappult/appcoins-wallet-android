package com.asfoundation.wallet.repository

interface PreferenceRepositoryType {

  fun hasCompletedOnboarding(): Boolean
  fun setOnboardingComplete()
  fun hasClickedSkipOnboarding(): Boolean
  fun setOnboardingSkipClicked()
  fun getCurrentWalletAddress(): String?
  fun setCurrentWalletAddress(address: String)
  fun isFirstTimeOnTransactionActivity(): Boolean
  fun setFirstTimeOnTransactionActivity()
  fun getPoaNotificationSeenTime(): Long
  fun clearPoaNotificationSeenTime()
  fun setPoaNotificationSeenTime(currentTimeInMillis: Long)
  fun setWalletValidationStatus(walletAddress: String, validated: Boolean)
  fun isWalletValidated(walletAddress: String): Boolean
  fun removeWalletValidationStatus(walletAddress: String)
  fun addWalletPreference(address: String?)
}
