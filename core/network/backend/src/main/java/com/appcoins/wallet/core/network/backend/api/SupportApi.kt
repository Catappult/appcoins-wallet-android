package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.IntercomAttributesRequest
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SupportApi {
  @POST("transaction/wallet/intercom/tag")
  fun setConversationAttributesAndTags(
    @Header("authorization") authorization: String,
    @Body attributes: IntercomAttributesRequest
  ): Single<Boolean>
}