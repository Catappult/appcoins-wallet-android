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
    if (applicationContext !is BdsApiProvider) {
      throw IllegalArgumentException(
          "application must implement ${BdsApiProvider::class.java.simpleName}")
    }
  }

  override fun onBind(intent: Intent): IBinder {
    return AppcoinsBillingBinder(BdsBilling(
        BdsRepository(RemoteRepository((applicationContext as BdsApiProvider).getBdsApi()))))
  }
}