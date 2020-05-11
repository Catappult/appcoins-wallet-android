package com.asfoundation.wallet.di

import com.asfoundation.wallet.support.AlarmManagerBroadcastReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverModule {

  @ContributesAndroidInjector
  abstract fun contributesAlarmManagerBroadcastReceiver(): AlarmManagerBroadcastReceiver

}