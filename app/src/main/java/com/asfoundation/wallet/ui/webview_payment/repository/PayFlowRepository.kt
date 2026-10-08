package com.asfoundation.wallet.ui.webview_payment.repository

import android.util.Log
import com.appcoins.wallet.core.network.microservices.api.payflow.PayFlowApi
import com.appcoins.wallet.core.network.microservices.model.PayFlowResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import io.reactivex.Single
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PayFlowRepository @Inject constructor(
  private val payFlowApi: PayFlowApi,
  private val rxSchedulers: RxSchedulers,
  private val logger: Logger,
) {

  fun getPayFlow(
    packageName: String,
    oemid: String?,
    appVersionCode: Int?,
  ): Single<PayFlowResult> {
    return payFlowApi.getPayFlow(
      packageName = packageName,
      oemid = oemid?.takeIf { it.isNotEmpty() },
      appVersionCode = appVersionCode
    )
      .subscribeOn(rxSchedulers.io)
      .doOnSuccess { registerEventIfInvalid(it) }
      .map<PayFlowResult> { PayFlowResult.Success(it) }
      .onErrorReturn {
        logger.log("PayFlow", "error in getPayFlow: ${it.message}", it)
        Log.d("PayFlowRepository", "error in getPayFlow: ${it.message}")
        if (it.isUnreachable()) PayFlowResult.Unreachable(it)
        else PayFlowResult.Success(PayFlowResponse(null))
      }
  }

  // Timeouts, connectivity failures and server errors mean the pay flow could not be decided,
  // as opposed to the backend explicitly answering which flow to use.
  private fun Throwable.isUnreachable(): Boolean =
    this is IOException || (this is HttpException && code() >= 500)

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

sealed class PayFlowResult {
  data class Success(val response: PayFlowResponse) : PayFlowResult()
  data class Unreachable(val throwable: Throwable) : PayFlowResult()
}
