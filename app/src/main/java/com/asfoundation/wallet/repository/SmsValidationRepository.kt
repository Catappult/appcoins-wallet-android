package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.ValidationCodeResponse
import com.asfoundation.wallet.entity.WalletStatus
import com.asfoundation.wallet.service.SmsValidationApi
import io.reactivex.Single
import retrofit2.HttpException

class SmsValidationRepository(
    private val api: SmsValidationApi
) : SmsValidationRepositoryType {

  override fun isValid(walletAddress: String): Single<Status> {
    return api.isValid(walletAddress)
        .map(this::mapResponse)
        .onErrorReturn(this::mapError)
  }

  override fun requestValidationCode(phoneNumber: String): Single<ValidationCodeResponse> {
    return api.requestValidationCode(phoneNumber)
  }

  override fun validateCode(phoneNumber: String, walletAddress: String,
                            validationCode: String): Single<Status> {
    return api.validateCode(phoneNumber, walletAddress, validationCode)
        .map(this::mapResponse)
        .onErrorReturn(this::mapError)
  }

  enum class Status {
    VERIFIED, UNVERIFIED, INVALID_PARAMS, TOO_MANY_REQUESTS, GENERAL_ERROR
  }

  private fun mapResponse(walletStatus: WalletStatus): Status {
    return if (walletStatus.verified) Status.VERIFIED else Status.UNVERIFIED
  }

  private fun mapError(throwable: Throwable): Status {
    return when (throwable) {
      is HttpException -> {
        when {
          throwable.code() == 400 -> Status.INVALID_PARAMS
          throwable.code() == 429 -> Status.TOO_MANY_REQUESTS
          else -> Status.GENERAL_ERROR
        }
      }
      else -> Status.GENERAL_ERROR
    }
  }

}