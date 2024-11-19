package com.asfoundation.wallet.iab.di

import android.app.Application
import android.content.Context
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.asfoundation.wallet.iab.presentation.payment_methods.credit_card.Adyen3DS2ComponentProvider
import com.asfoundation.wallet.iab.presentation.payment_methods.credit_card.RedirectComponentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityRetainedComponent::class)
class AdyenActivityRetainedModule {

  @Provides
  fun provideRedirectComponent(
    @ApplicationContext context: Context,
    redirectConfiguration: RedirectConfiguration,
  ): RedirectComponentProvider = RedirectComponentProvider {
    RedirectComponent.PROVIDER.get(it, context as Application, redirectConfiguration)
  }

  @Provides
  fun provideAdyen3DS2ComponentProvider(
    @ApplicationContext context: Context,
    adyen3DS2Configuration: Adyen3DS2Configuration,
  ): Adyen3DS2ComponentProvider = Adyen3DS2ComponentProvider {
    Adyen3DS2Component.PROVIDER.get(it, context as Application, adyen3DS2Configuration)
  }

}
