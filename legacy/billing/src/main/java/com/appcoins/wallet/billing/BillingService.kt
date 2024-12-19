package com.appcoins.wallet.billing

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.appcoins.wallet.bdsbilling.BdsBilling
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.BillingFactory
import com.appcoins.wallet.bdsbilling.BillingThrowableCodeMapper
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.BdsApiResponseMapper
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.InAppMapper
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.SubscriptionsMapper
import io.reactivex.schedulers.Schedulers

class BillingService : Service() {
  override fun onCreate() {
    super.onCreate()
    if (applicationContext !is BillingDependenciesProvider) {
      throw IllegalArgumentException(
        "application must implement ${BillingDependenciesProvider::class.java.simpleName}"
      )
    }
  }

  override fun onBind(intent: Intent): IBinder {
    val dependenciesProvider = applicationContext as BillingDependenciesProvider
    val serializer = ExternalBillingSerializer()
    return AppcoinsBillingBinder(
      supportedApiVersion = dependenciesProvider.supportedVersion(),
      billingMessagesMapper = dependenciesProvider.billingMessagesMapper(),
      packageManager = packageManager,
      billingFactory = object : BillingFactory {
        override fun getBilling(): Billing = BdsBilling(
          repository = BdsRepository(
            RemoteRepository(
              brokerBdsApi = dependenciesProvider.brokerBdsApi(),
              inappApi = dependenciesProvider.inappApi(),
              responseMapper = BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper()),
              subsApi = dependenciesProvider.subscriptionsApi(),
              rxSchedulers = dependenciesProvider.rxSchedulers(),
              fiatCurrenciesPreferences = dependenciesProvider.fiatCurrenciesPreferencesDataSource()
            )
          ),
          walletService = dependenciesProvider.walletService(),
          errorMapper = BillingThrowableCodeMapper(),
          partnerAddressService = dependenciesProvider.partnerAddressService()
        )
      },
      serializer = serializer,
      proxyService = dependenciesProvider.proxyService(),
      intentBuilder = BillingIntentBuilder(applicationContext),
      networkScheduler = Schedulers.io()
    )
  }
}