package com.asfoundation.wallet.ui.webview_login.repository

import android.util.Log
import com.appcoins.wallet.core.network.backend.api.LoginApi
import com.appcoins.wallet.core.network.microservices.api.payflow.PayFlowApi
import com.appcoins.wallet.core.network.microservices.model.PayFlowResponse
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import io.reactivex.Single
import javax.inject.Inject

class LoginRepository @Inject constructor(
  private val loginApi: LoginApi,
  private val rxSchedulers: RxSchedulers,
  private val logger: Logger,
) {

  fun fetchUserKey(
    authToken: String,
  ): Single<String> {  //TODO model
    return loginApi.fetchUserKey(
      jwt = authToken,
    )
      .subscribeOn(rxSchedulers.io)
      .doOnSuccess { /*TODO*/ }
//      .onErrorReturn {
//        logger.log("PayFlow", "error in getPayFlow: ${it.message}", it)
//        Log.d("PayFlowRepository", "error in getPayFlow: ${it.message}")
//        PayFlowResponse(null)
//      }
  }


}