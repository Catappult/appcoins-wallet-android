package com.appcoins.wallet.billing

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.appcoins.wallet.bdsbilling.BdsBilling
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.BillingFactory
import com.appcoins.wallet.bdsbilling.BillingThrowableCodeMapper
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.*
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
      dependenciesProvider.supportedVersion(),
      dependenciesProvider.billingMessagesMapper(),
      packageManager,
      object : BillingFactory {
        override fun getBilling(): Billing = BdsBilling(
          BdsRepository(
            RemoteRepository(
              dependenciesProvider.brokerBdsApi(),
              dependenciesProvider.inappApi(),
              BdsApiResponseMapper(SubscriptionsMapper(), InAppMapper()),
              dependenciesProvider.bdsApiSecondary(),
              dependenciesProvider.subscriptionsApi(),
              dependenciesProvider.ewtObtainer(),
              dependenciesProvider.rxSchedulers()
            )
          ),
          dependenciesProvider.walletService(),
          BillingThrowableCodeMapper()
        )
      },
      serializer,
      dependenciesProvider.proxyService(),
      BillingIntentBuilder(applicationContext),
      Schedulers.io()
    )
  }
}