package com.asfoundation.wallet.verification.ui.credit_card.code

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.appcoins.wallet.core.network.microservices.model.VerificationInfoResponse
import com.appcoins.wallet.billing.util.Error
import com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository
import com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor
import io.reactivex.Single
import javax.inject.Inject

class VerificationCodeInteractor @Inject constructor(
    private val walletVerificationInteractor: com.appcoins.wallet.feature.walletInfo.data.verification.WalletVerificationInteractor,
    private val brokerVerificationRepository: com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository,
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
      response: VerificationInfoResponse
  ): VerificationInfoModel {
    return VerificationInfoModel(System.currentTimeMillis(), response.format, response.value,
        response.currency, response.symbol, response.period, response.digits)
  }

}