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
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import io.reactivex.schedulers.Schedulers

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
    return AppcoinsBillingBinder(dependenciesProvider.supportedVersion(),
        dependenciesProvider.billingMessagesMapper(),
        packageManager,
        object : BillingFactory {
          override fun getBilling(): Billing {
            return BdsBilling(BdsRepository(
                RemoteRepository(dependenciesProvider.bdsApi(), BdsApiResponseMapper(),
                    dependenciesProvider.bdsApiSecondary())),
                dependenciesProvider.walletService(),
                BillingThrowableCodeMapper())
          }
        }, ExternalBillingSerializer(), dependenciesProvider.proxyService(),
        BillingIntentBuilder(applicationContext), Schedulers.io())
  }
}