package com.asfoundation.wallet.ui.backup

import androidx.appcompat.app.AppCompatActivity
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.ui.backup.BackupActivity.Companion.WALLET_ADDRESS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
class BackupActivityModule {

  @Provides
  fun providesBackupActivityPresenter(activity: AppCompatActivity,
                                      data: BackupActivityData,
                                      navigator: BackupActivityNavigator,
                                      walletsEventSender: WalletsEventSender): BackupActivityPresenter {
    return BackupActivityPresenter(activity as BackupActivityView, data, navigator,
        walletsEventSender)
  }

  @Provides
  fun providesBackupActivityData(activity: AppCompatActivity): BackupActivityData {
    activity.intent.extras!!.apply {
      return BackupActivityData(getString(WALLET_ADDRESS)!!)
    }
  }

  @Provides
  fun providesBackupActivityNavigator(activity: AppCompatActivity): BackupActivityNavigator {
    return BackupActivityNavigator(activity.supportFragmentManager, activity)
  }
}