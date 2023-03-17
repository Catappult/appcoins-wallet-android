package com.appcoins.wallet.core.network.eskills.api

import com.appcoins.wallet.core.network.eskills.model.LoginRequest
import com.appcoins.wallet.core.network.eskills.model.LoginResponse
import com.appcoins.wallet.core.network.eskills.model.RoomResponse
import io.reactivex.Single
import retrofit2.http.*

interface RoomApi {

  @GET("room/")
  fun getRoom(@Header("authorization") authorization: String): Single<RoomResponse>

  @POST("room/authorization/login")
  fun login(
    @Header("authorization") authorization: String,
    @Body loginRequest: LoginRequest
  ): Single<LoginResponse>
}
