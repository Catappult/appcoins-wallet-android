package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.WalletStatus
import com.asfoundation.wallet.entity.WalletValidationException
import com.asfoundation.wallet.service.SmsValidationApi
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Single

class SmsValidationRepository(
    private val api: SmsValidationApi
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
      is WalletValidationException -> {
        when {
          throwable.status == "INVALID_INPUT" -> WalletValidationStatus.INVALID_INPUT
          throwable.status == "INVALID_PHONE" -> WalletValidationStatus.INVALID_PHONE
          throwable.status == "DOUBLE_SPENT" -> WalletValidationStatus.DOUBLE_SPENT
          else -> WalletValidationStatus.GENERIC_ERROR
        }
      }
      else -> WalletValidationStatus.GENERIC_ERROR
    }
  }

}