package com.asfoundation.wallet.ui.webview_payment.repository

import android.util.Log
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.microservices.api.payflow.PayFlowApi
import com.appcoins.wallet.core.network.microservices.model.PayFlowResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Single
import javax.inject.Inject

class PayFlowRepository @Inject constructor(
  private val payFlowApi: PayFlowApi,
  private val rxSchedulers: RxSchedulers,
) {

  fun getPayFlow(
    packageName: String,
    packageVercode: Int,
    oemid: String,
  ): Single<PayFlowResponse> {
    return payFlowApi.getPayFlow(
      packageName = packageName,
      packageVercode = packageVercode,
      oemid = oemid,
    )
      .subscribeOn(rxSchedulers.io)
      .onErrorReturn {
        Log.i("PayFlowRepository", "error in getPayFlow: ${it.message}")
        PayFlowResponse(null)
      }
  }

}