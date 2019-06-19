package com.asfoundation.wallet.service

import com.asfoundation.wallet.entity.WalletRequestCodeResponse
import com.asfoundation.wallet.entity.WalletStatus
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface SmsValidationApi {

  @FormUrlEncoded
  @GET("transaction/verified_wallet")
  fun isValid(@Field("wallet") wallet: String): Single<WalletStatus>

  @FormUrlEncoded
  @POST("transaction/request_code")
  fun requestValidationCode(@Field("phone") phoneNumber: String): Single<WalletRequestCodeResponse>

  @FormUrlEncoded
  @POST("transaction/verify_code")
  fun validateCode(
      @Field("phone") phoneNumber: String,
      @Field("wallet") walletAddress: String,
      @Field("code") validationCode: String
  ): Single<WalletStatus>

}