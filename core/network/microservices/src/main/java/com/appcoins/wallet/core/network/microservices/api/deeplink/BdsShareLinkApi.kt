package com.appcoins.wallet.core.network.microservices.api.deeplink

import com.appcoins.wallet.core.network.microservices.model.GetPaymentLinkResponse
import com.appcoins.wallet.core.network.microservices.model.ShareLinkData
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST


interface BdsShareLinkApi {
  @POST("8.20190326/topup/inapp/products")
  fun getPaymentLink(@Body data: ShareLinkData): Single<GetPaymentLinkResponse>
}
