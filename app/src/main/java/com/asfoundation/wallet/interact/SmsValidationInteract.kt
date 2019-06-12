package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.ValidationCodeResponse
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.SmsValidationRepository
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import io.reactivex.Single

class SmsValidationInteract(
    private val smsValidationRepository: SmsValidationRepositoryType
) {

  fun isValid(wallet: Wallet): Single<SmsValidationRepository.Status> {
    return smsValidationRepository.isValid(wallet.address)
  }

  fun requestValidationCode(phoneNumber: String): Single<ValidationCodeResponse> {
    return smsValidationRepository.requestValidationCode(phoneNumber)
  }

  fun validateCode(phoneNumber: String, wallet: Wallet,
                   code: String): Single<SmsValidationRepository.Status> {
    return smsValidationRepository.validateCode(phoneNumber, wallet.address, code)
  }

}