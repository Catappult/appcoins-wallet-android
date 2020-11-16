package com.asfoundation.wallet.wallet_verification

import dagger.Module
import dagger.Provides

@Module
class WalletVerificationActivityModule {

  @Provides
  fun providesWalletVerificationActivityPresenter(
      navigator: WalletVerificationActivityNavigator): WalletVerificationActivityPresenter {
    return WalletVerificationActivityPresenter(navigator)
  }

  @Provides
  fun providesWalletVerificationActivityNavigator(
      activity: WalletVerificationActivity): WalletVerificationActivityNavigator {
    return WalletVerificationActivityNavigator(activity, activity.supportFragmentManager)
  }
}