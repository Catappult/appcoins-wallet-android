package com.asfoundation.wallet.wallet_verification.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import io.reactivex.Single

class WalletVerificationCodeInteractor(
    private val adyenPaymentRepository: AdyenPaymentRepository,
    private val walletService: WalletService
) {

  fun confirmCode(code: String): Single<VerificationCodeResult> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          adyenPaymentRepository.validateCode(code, it.address, it.signedAddress)
        }
  }

}