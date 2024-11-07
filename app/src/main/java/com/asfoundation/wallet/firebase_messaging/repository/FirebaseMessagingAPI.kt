package com.asfoundation.wallet.firebase_messaging.repository

import com.asfoundation.wallet.firebase_messaging.repository.model.FirebaseTokenData
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FirebaseMessagingAPI {

  @POST("/appc/firebase_token")
  fun registerToken(
    @Header("authorization") authorization: String,
    @Body firebaseTokenData: FirebaseTokenData
  ): Completable

  @DELETE("/appc/firebase_token/{token}")
  fun unregisterToken(
    @Header("authorization") authorization: String,
    @Path("token") token: String,
  ): Completable
}
