package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.PreferenceRepositoryType
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Single

class SmsValidationInteract(
    private val smsValidationRepository: SmsValidationRepositoryType,
    private val preferenceRepositoryType: PreferenceRepositoryType
) {

  fun isValid(wallet: Wallet): Single<WalletValidationStatus> {
    return smsValidationRepository.isValid(wallet.address)
        .doOnSuccess { saveWalletVerifiedStatus(it, wallet.address) }
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
      preferenceRepositoryType.setWalletValidationStatus(walletAddress, true)
    } else if (status == WalletValidationStatus.GENERIC_ERROR) {
      preferenceRepositoryType.setWalletValidationStatus(walletAddress, false)
    }
  }

}