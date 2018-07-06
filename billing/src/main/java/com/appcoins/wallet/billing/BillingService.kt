package com.appcoins.wallet.billing

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.appcoins.wallet.billing.repository.BdsRepository

class BillingService : Service() {
  companion object {
    private val TAG: String = BillingService::class.java.simpleName
  }

  override fun onBind(intent: Intent): IBinder {
    Log.d(TAG, "onBind() called with: intent = [$intent]")
    return AppcoinsBillingBinder(BdsBilling(
        BdsRepository()))
  }
}