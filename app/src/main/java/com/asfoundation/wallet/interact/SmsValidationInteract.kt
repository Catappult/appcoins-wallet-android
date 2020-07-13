package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Single

class SmsValidationInteract(
    private val smsValidationRepository: SmsValidationRepositoryType,
    private val preferencesRepositoryType: PreferencesRepositoryType
) {

  fun isValidated(address: String): Single<Boolean> {
    return getValidationStatus(address).map { it == WalletValidationStatus.SUCCESS }
  }

  fun getValidationStatus(address: String): Single<WalletValidationStatus> {
    return smsValidationRepository.isValid(address)
        .doOnSuccess { saveWalletVerifiedStatus(it, address) }
  }

  fun requestValidationCode(phoneNumber: String): Single<WalletValidationStatus> {
    return smsValidationRepository.requestValidationCode(phoneNumber)
  }

  fun validateCode(phoneNumber: String, wallet: Wallet,
                   code: String): Single<WalletValidationStatus> {
    return smsValidationRepository.validateCode(phoneNumber, wallet.address, code)
        .doOnSuccess { saveWalletVerifiedStatus(it, wallet.address) }
  }

  private fun saveWalletVerifiedStatus(status: WalletValidationStatus, walletAddress: String) {
    if (status == WalletValidationStatus.DOUBLE_SPENT || status == WalletValidationStatus.SUCCESS) {
      preferencesRepositoryType.setWalletValidationStatus(walletAddress, true)
    } else if (status == WalletValidationStatus.GENERIC_ERROR) {
      preferencesRepositoryType.setWalletValidationStatus(walletAddress, false)
    }
  }

}