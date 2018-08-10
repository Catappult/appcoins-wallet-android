package com.appcoins.wallet.billing

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer
import com.appcoins.wallet.billing.repository.BdsApiResponseMapper
import com.appcoins.wallet.billing.repository.BdsRepository
import com.appcoins.wallet.billing.repository.RemoteRepository

class BillingService : Service() {
  override fun onCreate() {
    super.onCreate()
    if (applicationContext !is BillingDependenciesProvider) {
      throw IllegalArgumentException(
          "application must implement ${BillingDependenciesProvider::class.java.simpleName}")
    }
  }

  override fun onBind(intent: Intent): IBinder {
      val dependenciesProvider = applicationContext as BillingDependenciesProvider
      return AppcoinsBillingBinder(dependenciesProvider.getSupportedVersion(),
              BillingMessagesMapper(),
        packageManager,
        object : BillingFactory {
          override fun getBilling(merchantName: String): Billing {
            return BdsBilling(merchantName,
                    BdsRepository(
                            RemoteRepository(dependenciesProvider.getBdsApi(), BdsApiResponseMapper()),
                            BillingThrowableCodeMapper()), dependenciesProvider.getWalletService(),
                BillingThrowableCodeMapper())
          }
        }, ExternalBillingSerializer(), dependenciesProvider.getProxyService(),
        BillingIntentBuilder(applicationContext))
  }
}