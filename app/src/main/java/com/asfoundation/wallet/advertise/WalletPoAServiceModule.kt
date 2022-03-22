package com.asfoundation.wallet.advertise

import android.app.Service
import com.asfoundation.wallet.main.MainActivityNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

@InstallIn(ServiceComponent::class)
@Module
class WalletPoAServiceModule {
  @Provides
  fun provideHomeNavigator(service: Service): MainActivityNavigator {
    return MainActivityNavigator(service)
  }
}