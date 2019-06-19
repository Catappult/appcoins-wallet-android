package com.asfoundation.wallet.service

import com.asfoundation.wallet.entity.ValidationCodeResponse
import com.asfoundation.wallet.entity.WalletStatus
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SmsValidationApi {

  @GET("transaction/verified_wallet")
  fun isValid(@Query("wallet") wallet: String): Single<WalletStatus>

  @POST("transaction/request_code")
  fun requestValidationCode(@Field("phone") phoneNumber: String): Single<ValidationCodeResponse>

  @POST("transaction/verify_code")
  fun validateCode(
      @Field("phone") phoneNumber: String,
      @Field("code") validationCode: String,
      @Field("wallet") walletAddress: String
  ): Single<WalletStatus>

}