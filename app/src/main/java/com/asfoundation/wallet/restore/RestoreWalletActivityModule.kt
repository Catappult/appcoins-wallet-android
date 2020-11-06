package com.asfoundation.wallet.restore

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
class RestoreWalletActivityModule {

  @Provides
  fun providesRestoreWalletActivityPresenter(view: RestoreWalletActivityView,
                                             walletsEventSender: WalletsEventSender,
                                             navigator: RestoreWalletActivityNavigator): RestoreWalletActivityPresenter {
    return RestoreWalletActivityPresenter(view, walletsEventSender, navigator)
  }

  @Provides
  fun providesRestoreWalletActivityNavigator(
      activity: RestoreWalletActivity): RestoreWalletActivityNavigator {
    return RestoreWalletActivityNavigator(activity, activity.supportFragmentManager)
  }
}

@Module
abstract class RestoreWalletActivityViewModule {
  @Binds
  abstract fun providesRestoreWalletActivityView(
      activity: RestoreWalletActivity): RestoreWalletActivityView
}