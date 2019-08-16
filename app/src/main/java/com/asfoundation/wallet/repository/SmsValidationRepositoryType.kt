package com.asfoundation.wallet.repository

import com.asfoundation.wallet.poa_wallet_validation.WalletValidationStatus
import io.reactivex.Single

interface SmsValidationRepositoryType {

  fun isValid(walletAddress: String): Single<WalletValidationStatus>

  fun requestValidationCode(phoneNumber: String): Single<WalletValidationStatus>

  fun validateCode(phoneNumber: String, walletAddress: String,
                   validationCode: String): Single<WalletValidationStatus>

}