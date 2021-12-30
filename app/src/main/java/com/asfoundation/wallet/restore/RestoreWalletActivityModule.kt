package com.asfoundation.wallet.restore

import androidx.appcompat.app.AppCompatActivity
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.main.MainActivityNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
class RestoreWalletActivityModule {

  @Provides
  fun providesRestoreWalletActivityPresenter(activity: AppCompatActivity,
                                             walletsEventSender: WalletsEventSender,
                                             navigator: RestoreWalletActivityNavigator): RestoreWalletActivityPresenter {
    return RestoreWalletActivityPresenter(activity as RestoreWalletActivityView, walletsEventSender,
        navigator)
  }

  @Provides
  fun providesRestoreWalletActivityNavigator(
      activity: AppCompatActivity): RestoreWalletActivityNavigator {
    return RestoreWalletActivityNavigator(MainActivityNavigator(activity),
        activity.supportFragmentManager)
  }
}