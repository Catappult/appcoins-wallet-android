package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.ValidationCodeResponse
import com.asfoundation.wallet.entity.WalletStatus
import com.asfoundation.wallet.service.SMSValidationApi
import io.reactivex.Single
import rx.exceptions.Exceptions

class SMSValidationRepository(
        private val api: SMSValidationApi
) : SMSValidationRepositoryType {

    override fun validateWallet(walletAddress: String): Single<WalletStatus> {
        return api.validateWallet(walletAddress)
                .doOnSuccess(this::handleErrors)
    }

    override fun requestValidationCode(phoneNumber: String): Single<ValidationCodeResponse> {
        return api.requestValidationCode(phoneNumber)
    }

    override fun validateCode(phoneNumber: String, walletAddress: String, validationCode: String): Single<WalletStatus> {
        return api.validateCode(phoneNumber, validationCode, walletAddress)
    }

    private fun handleErrors(walletStatus: WalletStatus) {
        if (!walletStatus.verified) Exceptions.propagate(Throwable("Wallet is not verified"))
    }

}