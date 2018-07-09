package com.appcoins.wallet.billing

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.appcoins.wallet.billing.repository.BdsRepository
import com.appcoins.wallet.billing.repository.RemoteRepository
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class BillingService : Service() {
  companion object {
    private val TAG: String = BillingService::class.java.simpleName
  }

  override fun onBind(intent: Intent): IBinder {
    Log.d(TAG, "onBind() called with: intent = [$intent]")
    val bdsApi = Retrofit.Builder().baseUrl(RemoteRepository.BASE_HOST)
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(RemoteRepository.BdsApi::class.java)
    return AppcoinsBillingBinder(BdsBilling(BdsRepository(RemoteRepository(bdsApi))))
  }
}