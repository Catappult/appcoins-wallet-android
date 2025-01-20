package com.asfoundation.wallet.firebase_messaging.repository

import com.asfoundation.wallet.firebase_messaging.repository.model.FirebaseTokenData
import com.asfoundation.wallet.firebase_messaging.repository.model.TokenResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FirebaseMessagingAPI {

  @POST("/appc/firebase_token")
  fun registerToken(
    @Header("Authorization") jwt: String,
    @Body firebaseTokenData: FirebaseTokenData
  ): Single<TokenResponse>

  @DELETE("/appc/firebase_token/{token}")
  fun unregisterToken(
    @Header("Authorization") jwt: String,
    @Path("token") token: String,
  ): Single<TokenResponse>
}
