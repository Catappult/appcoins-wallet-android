package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.WalletStatus
import com.asfoundation.wallet.entity.WalletValidationException
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.service.SmsValidationApi
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import com.google.gson.Gson
import io.reactivex.Single
import retrofit2.HttpException

class SmsValidationRepository(
    private val api: SmsValidationApi,
    private val gson: Gson,
    private val logger: Logger
) : SmsValidationRepositoryType {

  companion object {
    private val TAG = SmsValidationRepository::class.java.simpleName
  }

  override fun isValid(walletAddress: String): Single<WalletValidationStatus> {
    return api.isValid(walletAddress)
        .map(this::mapValidationResponse)
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
        .map(this::mapCodeValidationResponse)
        .onErrorReturn(this::mapError)
  }

  private fun mapValidationResponse(walletStatus: WalletStatus): WalletValidationStatus {
    return if (walletStatus.verified) WalletValidationStatus.SUCCESS else WalletValidationStatus.GENERIC_ERROR
  }

  private fun mapCodeValidationResponse(walletStatus: WalletStatus): WalletValidationStatus {
    return if (walletStatus.verified) WalletValidationStatus.SUCCESS else WalletValidationStatus.INVALID_INPUT
  }

  private fun mapError(throwable: Throwable): WalletValidationStatus {
    return when (throwable) {
      is HttpException -> {
        var walletValidationException = WalletValidationException("")
        if (throwable.code() == 400) {
          throwable.response()
              ?.errorBody()
              ?.charStream()
              ?.let {
                walletValidationException = gson.fromJson(it, WalletValidationException::class.java)
              } ?: logger.log(TAG, throwable.message(), throwable)
        }
        when {
          throwable.code() == 400 -> {
            when (walletValidationException.status) {
              "INVALID_INPUT" -> WalletValidationStatus.INVALID_INPUT
              "INVALID_CODE" -> WalletValidationStatus.INVALID_CODE
              "INVALID_PHONE" -> WalletValidationStatus.INVALID_PHONE
              "DOUBLE_SPENT" -> WalletValidationStatus.DOUBLE_SPENT
              "REGION_NOT_SUPPORTED" -> WalletValidationStatus.REGION_NOT_SUPPORTED
              "LANDLINE_NOT_SUPPORTED" -> WalletValidationStatus.LANDLINE_NOT_SUPPORTED
              "EXPIRED_CODE" -> WalletValidationStatus.EXPIRED_CODE
              else -> WalletValidationStatus.GENERIC_ERROR
            }
          }
          throwable.code() == 429 -> {
            //TODO what happens to DOUBLE SPENT?
            WalletValidationStatus.TOO_MANY_ATTEMPTS
          }
          else -> WalletValidationStatus.GENERIC_ERROR
        }
      }
      else -> {
        if (throwable.isNoNetworkException()) {
          WalletValidationStatus.NO_NETWORK
        } else {
          WalletValidationStatus.GENERIC_ERROR
        }
      }
    }
  }

}