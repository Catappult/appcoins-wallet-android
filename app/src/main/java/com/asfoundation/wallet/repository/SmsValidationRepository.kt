package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.WalletStatus
import com.asfoundation.wallet.entity.WalletValidationException
import com.asfoundation.wallet.service.SmsValidationApi
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import com.google.gson.Gson
import io.reactivex.Single
import retrofit2.HttpException

class SmsValidationRepository(
    private val api: SmsValidationApi,
    private val gson: Gson
) : SmsValidationRepositoryType {

  override fun isValid(walletAddress: String): Single<WalletValidationStatus> {
    return api.isValid(walletAddress)
        .map(this::mapResponse)
        .onErrorReturn(this::mapError)
  }

  override fun requestValidationCode(phoneNumber: String): Single<WalletValidationStatus> {
    return api.requestValidationCode(phoneNumber)
        .map { WalletValidationStatus.SUCCESS }
        .onErrorReturn(this::mapError)
  }

  override fun validateCode(phoneNumber: String, walletAddress: String,
                            validationCode: String): Single<WalletValidationStatus> {
    return api.validateCode(phoneNumber, walletAddress, validationCode)
        .map(this::mapResponse)
        .onErrorReturn(this::mapError)
  }

  private fun mapResponse(walletStatus: WalletStatus): WalletValidationStatus {
    return if (walletStatus.verified) WalletValidationStatus.SUCCESS else WalletValidationStatus.GENERIC_ERROR
  }

  private fun mapError(throwable: Throwable): WalletValidationStatus {
    return when (throwable) {
      is HttpException -> {
        val walletValidationException = gson.fromJson(throwable.response()
            .errorBody()!!
            .charStream(), WalletValidationException::class.java)
        when {
          throwable.code() == 400 && walletValidationException.status == "INVALID_INPUT" -> WalletValidationStatus.INVALID_INPUT
          throwable.code() == 400 && walletValidationException.status == "INVALID_PHONE" -> WalletValidationStatus.INVALID_PHONE
          throwable.code() == 400 && walletValidationException.status == "DOUBLE_SPENT" -> WalletValidationStatus.DOUBLE_SPENT
          throwable.code() == 429 -> WalletValidationStatus.DOUBLE_SPENT
          else -> WalletValidationStatus.GENERIC_ERROR
        }
      }
      else -> WalletValidationStatus.GENERIC_ERROR
    }
  }

}