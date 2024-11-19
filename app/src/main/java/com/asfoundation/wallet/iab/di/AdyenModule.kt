package com.asfoundation.wallet.iab.di

import android.content.Context
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.asf.wallet.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AdyenModule {

  @Provides
  @Singleton
  fun provideCardConfiguration(
    @ApplicationContext context: Context,
    adyenEnvironment: Environment,
  ): CardConfiguration =
    CardConfiguration.Builder(context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment)
      .build()

  @Provides
  @Singleton
  fun provideRedirectConfiguration(
    @ApplicationContext context: Context,
    adyenEnvironment: Environment,
  ): RedirectConfiguration =
    RedirectConfiguration.Builder(context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment)
      .build()

  @Provides
  @Singleton
  fun provideAdyen3DS2Configuration(
    @ApplicationContext context: Context,
    adyenEnvironment: Environment,
  ): Adyen3DS2Configuration =
    Adyen3DS2Configuration.Builder(context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment)
      .build()

  @AdyenReturnUrl
  @Provides
  fun provideReturnUrl(
    @ApplicationContext context: Context,
  ): String = RedirectComponent.getReturnUrl(context)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AdyenReturnUrl
