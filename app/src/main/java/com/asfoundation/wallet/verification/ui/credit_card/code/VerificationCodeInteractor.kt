package com.asfoundation.wallet.verification.ui.credit_card.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.appcoins.wallet.core.network.microservices.api.VerificationInfoResponse
import com.appcoins.wallet.billing.util.Error
import com.asfoundation.wallet.verification.repository.BrokerVerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.WalletVerificationInteractor
import io.reactivex.Single
import javax.inject.Inject

class VerificationCodeInteractor @Inject constructor(
  private val walletVerificationInteractor: WalletVerificationInteractor,
  private val brokerVerificationRepository: BrokerVerificationRepository,
  private val walletService: WalletService
) {

  fun confirmCode(code: String): Single<VerificationCodeResult> {
    return walletVerificationInteractor.confirmVerificationCode(code)
  }

  fun loadVerificationIntroModel(): Single<VerificationInfoModel> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap {
          brokerVerificationRepository.getVerificationInfo(it.address, it.signedAddress)
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