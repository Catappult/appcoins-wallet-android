package com.asfoundation.wallet.iab.di

import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asfoundation.wallet.iab.parser.UriParser
import com.asfoundation.wallet.iab.parser.UriParserImpl
import com.asfoundation.wallet.iab.parser.osp.OSPUriParserImpl
import com.asfoundation.wallet.iab.parser.sdk.SDKUriParserImpl
import com.asfoundation.wallet.iab.payment_manager.PaymentMethodCreator
import com.asfoundation.wallet.iab.payment_manager.factory.APPCFactory
import com.asfoundation.wallet.iab.payment_manager.factory.AskSomeoneToPayFactory
import com.asfoundation.wallet.iab.payment_manager.factory.CarrierBillingFactory
import com.asfoundation.wallet.iab.payment_manager.factory.CodaPayFactory
import com.asfoundation.wallet.iab.payment_manager.factory.CreditCardFactory
import com.asfoundation.wallet.iab.payment_manager.factory.CreditCardWalletOneFactory
import com.asfoundation.wallet.iab.payment_manager.factory.FyberOfferwallFactory
import com.asfoundation.wallet.iab.payment_manager.factory.GooglePayFactory
import com.asfoundation.wallet.iab.payment_manager.factory.MiPayFactory
import com.asfoundation.wallet.iab.payment_manager.factory.PayPalV1Factory
import com.asfoundation.wallet.iab.payment_manager.factory.PayPalV2Factory
import com.asfoundation.wallet.iab.payment_manager.factory.SandboxFactory
import com.asfoundation.wallet.iab.payment_manager.factory.TrueLayerFactory
import com.asfoundation.wallet.iab.payment_manager.factory.UnknownPaymentMethodFactory
import com.asfoundation.wallet.iab.payment_manager.factory.VKFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Qualifier

@Module
@InstallIn(ActivityComponent::class)
interface IAPModule {

  @Binds
  @GenericUriParser
  fun bindUriParser(uriParserImpl: UriParserImpl): UriParser

  @Binds
  @OSPUriParser
  fun bindOSPUriParser(ospParserImpl: OSPUriParserImpl): UriParser

  @Binds
  @SDKUriParser
  fun bindSDKUriParser(sdkParserImpl: SDKUriParserImpl): UriParser

  companion object {

    @Provides
    fun providePaymentMethodCreator(
      creditCardFactory: CreditCardFactory,
      currencyFormatUtils: CurrencyFormatUtils
    ) =
      PaymentMethodCreator(
        paymentMethodFactories = listOf(
          APPCFactory(),
          creditCardFactory,
          PayPalV1Factory(),
          PayPalV2Factory(),
          VKFactory(),
          AskSomeoneToPayFactory(),
          CodaPayFactory(),
          GooglePayFactory(),
          MiPayFactory(),
          TrueLayerFactory(),
          CarrierBillingFactory(),
          FyberOfferwallFactory(),
          SandboxFactory(),
          CreditCardWalletOneFactory(),
          UnknownPaymentMethodFactory(), // Keep this factory in the last place. It will show payment methods not implemented as disabled. it will only be used if all the others fail to create a payment method.
        ),
        currencyFormatUtils = currencyFormatUtils
      )
  }

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GenericUriParser

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OSPUriParser

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SDKUriParser
