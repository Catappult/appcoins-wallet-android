package com.asfoundation.wallet.di

import com.asfoundation.wallet.backup.BackupBroadcastReceiver
import com.asfoundation.wallet.support.AlarmManagerBroadcastReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverBuilders {

  @ContributesAndroidInjector
  abstract fun contributesAlarmManagerBroadcastReceiver(): AlarmManagerBroadcastReceiver

  @ContributesAndroidInjector
  abstract fun contributesBackupBroadcastReceiver(): BackupBroadcastReceiver

}