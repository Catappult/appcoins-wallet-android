package com.asfoundation.wallet.verification.credit_card.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.appcoins.wallet.billing.adyen.VerificationInfoResponse
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.verification.credit_card.WalletVerificationInteractor
import io.reactivex.Single

class VerificationCodeInteractor(
    private val walletVerificationInteractor: WalletVerificationInteractor,
    private val adyenPaymentRepository: AdyenPaymentRepository,
    private val walletService: WalletService
) {

  fun confirmCode(code: String): Single<VerificationCodeResult> {
    return walletVerificationInteractor.confirmVerificationCode(code)
  }

  fun loadVerificationIntroModel(): Single<VerificationInfoModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          adyenPaymentRepository.getVerificationInfo(it.address, it.signedAddress)
              .map { info -> mapToVerificationInfoModel(info) }
        }
        .onErrorReturn { VerificationInfoModel(Error(true)) }
  }

  private fun mapToVerificationInfoModel(
      response: VerificationInfoResponse): VerificationInfoModel {
    return VerificationInfoModel(System.currentTimeMillis(), response.format, response.value,
        response.currency, response.symbol, response.period, response.digits)
  }

}