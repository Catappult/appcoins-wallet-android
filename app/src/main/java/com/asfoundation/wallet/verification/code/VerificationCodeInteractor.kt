package com.asfoundation.wallet.verification.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.appcoins.wallet.billing.adyen.VerificationInfoResponse
import com.asfoundation.wallet.verification.WalletVerificationInteractor
import io.reactivex.Single

class VerificationCodeInteractor(
    private val walletVerificationInteractor: WalletVerificationInteractor,
    private val adyenPaymentRepository: AdyenPaymentRepository,
    private val walletService: WalletService
) {

  fun confirmCode(code: String): Single<VerificationCodeResult> {
    return walletVerificationInteractor.confirmVerificationCode(code)
  }

  fun loadVerificationIntroModel(): Single<VerificationCodeData> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          adyenPaymentRepository.getVerificationInfo(it.address, it.signedAddress)
              .map { info -> mapToVerificationInfoModel(info) }
        }
  }

  private fun mapToVerificationInfoModel(response: VerificationInfoResponse): VerificationCodeData {
    return VerificationCodeData(true, System.currentTimeMillis(), response.format, response.value,
        response.currency, response.symbol, response.period, response.digits)
  }

}