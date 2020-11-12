package com.asfoundation.wallet.wallet_verification

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import dagger.Module
import dagger.Provides

@Module
class WalletVerificationActivityModule {

  @Provides
  fun providesWalletVerificationActivityPresenter(activity: WalletVerificationActivity,
                                                  walletsEventSender: WalletsEventSender,
                                                  navigator: WalletVerificationActivityNavigator): WalletVerificationActivityPresenter {
    return WalletVerificationActivityPresenter(activity as WalletVerificationActivityView,
        walletsEventSender,
        navigator)
  }

  @Provides
  fun providesWalletVerificationActivityNavigator(
      activity: WalletVerificationActivity): WalletVerificationActivityNavigator {
    return WalletVerificationActivityNavigator(activity, activity.supportFragmentManager)
  }
}