package com.asfoundation.wallet.repository

import io.reactivex.Completable
import io.reactivex.Single

interface PreferenceRepositoryType {

  fun hasCompletedOnboarding(): Boolean
  fun setOnboardingComplete()
  fun hasClickedSkipOnboarding(): Boolean
  fun setOnboardingSkipClicked()
  fun getCurrentWalletAddress(): String?
  fun setCurrentWalletAddress(address: String)
  fun isFirstTimeOnTransactionActivity(): Boolean
  fun setFirstTimeOnTransactionActivity()
  fun saveAutoUpdateCardDismiss(updateVersionCode: Int): Completable
  fun getAutoUpdateCardDismissedVersion(): Single<Int>

}
