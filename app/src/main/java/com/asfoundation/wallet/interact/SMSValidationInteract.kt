package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.ValidationCodeResponse
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.entity.WalletStatus
import com.asfoundation.wallet.repository.SMSValidationRepository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SMSValidationInteract(
        private val smsValidationRepository: SMSValidationRepository
) {

    fun validateWallet(wallet: Wallet): Single<WalletStatus> {
        return smsValidationRepository.validateWallet(wallet.address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun requestValidationCode(phoneNumber: String): Single<ValidationCodeResponse> {
        return smsValidationRepository.requestValidationCode(phoneNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun validateCode(phoneNumber: String, wallet: Wallet, code: String): Single<WalletStatus> {
        return smsValidationRepository.validateCode(phoneNumber, wallet.address, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}