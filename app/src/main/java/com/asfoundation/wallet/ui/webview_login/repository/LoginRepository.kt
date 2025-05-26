package com.asfoundation.wallet.ui.webview_login.repository

import com.appcoins.wallet.core.network.backend.api.LoginApi
import com.appcoins.wallet.core.network.backend.model.FetchUserKeyResponse
import com.appcoins.wallet.core.utils.android_common.Log
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
  ): Single<FetchUserKeyResponse> {
    return loginApi.fetchUserKey(
      jwt = "Bearer $authToken",
    )
      .subscribeOn(rxSchedulers.io)
      .doOnError {
        Log.d("LoginRepository", "error in LoginRepository: ${it.message}")
      }
  }

}
