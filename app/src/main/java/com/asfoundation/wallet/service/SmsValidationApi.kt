package com.asfoundation.wallet.service

import com.asfoundation.wallet.entity.WalletRequestCodeResponse
import com.asfoundation.wallet.entity.WalletStatus
import io.reactivex.Single
import retrofit2.http.*

interface SmsValidationApi {

  @GET("transaction/verified_wallet")
  fun isValid(@Query("wallet") wallet: String): Single<WalletStatus>

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