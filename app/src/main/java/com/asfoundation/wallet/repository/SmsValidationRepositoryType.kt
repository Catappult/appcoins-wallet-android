package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.ValidationCodeResponse
import io.reactivex.Single

interface SmsValidationRepositoryType {

  fun isValid(walletAddress: String): Single<SmsValidationRepository.Status>

  fun requestValidationCode(phoneNumber: String): Single<ValidationCodeResponse>

  fun validateCode(phoneNumber: String, walletAddress: String,
                   validationCode: String): Single<SmsValidationRepository.Status>

}