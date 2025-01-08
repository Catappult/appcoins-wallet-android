package com.asfoundation.wallet.ui.webview_payment.repository

import android.util.Log
import com.appcoins.wallet.core.network.microservices.api.payflow.PayFlowApi
import com.appcoins.wallet.core.network.microservices.model.PayFlowResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import io.reactivex.Single
import javax.inject.Inject

class PayFlowRepository @Inject constructor(
  private val payFlowApi: PayFlowApi,
  private val rxSchedulers: RxSchedulers,
  private val logger: Logger,
) {

  fun getPayFlow(
    packageName: String,
    oemid: String?,
  ): Single<PayFlowResponse> {
    return payFlowApi.getPayFlow(
      packageName = packageName,
      oemid = oemid?.takeIf { it.isNotEmpty() },
    )
      .subscribeOn(rxSchedulers.io)
      .doOnSuccess { registerEventIfInvalid(it) }
      .onErrorReturn {
        logger.log("PayFlow", "error in getPayFlow: ${it.message}", it)
        Log.d("PayFlowRepository", "error in getPayFlow: ${it.message}")
        PayFlowResponse(null)
      }
  }

  private fun registerEventIfInvalid(payFlowResponse: PayFlowResponse) {
    when {
      payFlowResponse.paymentMethods?.walletWebViewPayment != null -> { }
      payFlowResponse.paymentMethods?.walletApp != null -> { }
      else -> {
        logger.log("PayFlow", "invalid payFlowResponse: $payFlowResponse", Exception("invalid payFlowResponse"))
        Log.d("PayFlowRepository", "invalid payFlowResponse: $payFlowResponse")
      }
    }
  }

}