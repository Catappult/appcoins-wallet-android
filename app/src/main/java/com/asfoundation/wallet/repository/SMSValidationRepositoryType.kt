package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.ValidationCodeResponse
import com.asfoundation.wallet.entity.WalletStatus
import io.reactivex.Single

interface SMSValidationRepositoryType {

    fun validateWallet(walletAddress: String): Single<WalletStatus>

    fun requestValidationCode(phoneNumber: String): Single<ValidationCodeResponse>

    fun validateCode(phoneNumber: String, walletAddress: String, validationCode: String): Single<WalletStatus>

}