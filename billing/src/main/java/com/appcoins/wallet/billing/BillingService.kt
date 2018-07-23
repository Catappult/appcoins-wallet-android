package com.appcoins.wallet.billing

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.appcoins.wallet.billing.repository.BdsRepository
import com.appcoins.wallet.billing.repository.RemoteRepository

class BillingService : Service() {
  companion object {
    private val TAG: String = BillingService::class.java.simpleName
  }

  override fun onCreate() {
    super.onCreate()
    if (applicationContext !is BillingDependenciesProvider) {
      throw IllegalArgumentException(
          "application must implement ${BillingDependenciesProvider::class.java.simpleName}")
    }
  }

  override fun onBind(intent: Intent): IBinder {
    val dependeciesProvider = applicationContext as BillingDependenciesProvider
    return AppcoinsBillingBinder(BdsBilling(
        BdsRepository(RemoteRepository(dependeciesProvider.getBdsApi())), BillingThrowableCodeMapper()),
        dependeciesProvider.getSupportedVersion(), dependeciesProvider.getWalletService())
  }
}