package com.appcoins.wallet.billing

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.appcoins.wallet.bdsbilling.BdsBilling
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.BillingFactory
import com.appcoins.wallet.bdsbilling.BillingThrowableCodeMapper
import com.appcoins.wallet.bdsbilling.repository.BdsApiResponseMapper
import com.appcoins.wallet.bdsbilling.repository.BdsRepository
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.billing.mappers.ExternalBillingSerializer

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
        dependenciesProvider.getBillingMessagesMapper(),
        packageManager,
        object : BillingFactory {
          override fun getBilling(): Billing {
            return BdsBilling(BdsRepository(
                RemoteRepository(dependenciesProvider.getBdsApi(), BdsApiResponseMapper(),
                    dependenciesProvider.getBdsApiSecondary())),
                dependenciesProvider.getWalletService(),
                BillingThrowableCodeMapper())
          }
        }, ExternalBillingSerializer(), dependenciesProvider.getProxyService(),
        BillingIntentBuilder(applicationContext))
  }
}