package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.ValidationCodeResponse
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.repository.SmsValidationRepository
import com.asfoundation.wallet.repository.SmsValidationRepositoryType
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class SmsValidationInteract(
    private val smsValidationRepository: SmsValidationRepositoryType,
    private val scheduler: Scheduler
) {

  fun isValid(wallet: Wallet): Single<SmsValidationRepository.Status> {
    return smsValidationRepository.isValid(wallet.address)
        .subscribeOn(scheduler)
  }

  fun requestValidationCode(phoneNumber: String): Single<ValidationCodeResponse> {
    return smsValidationRepository.requestValidationCode(phoneNumber)
        .subscribeOn(scheduler)
  }

  fun validateCode(phoneNumber: String, wallet: Wallet,
                   code: String): Single<SmsValidationRepository.Status> {
    return smsValidationRepository.validateCode(phoneNumber, wallet.address, code)
        .subscribeOn(scheduler)
  }

}