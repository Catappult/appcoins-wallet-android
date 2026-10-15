package com.asfoundation.wallet.verification.ui.credit_card

import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import io.reactivex.Single
import javax.inject.Inject

class VerificationCreditCardActivityInteractor @Inject constructor(
  private val brokerVerificationRepository: BrokerVerificationRepository,
  private val walletService: WalletService
) {

  fun getVerificationStatus(): Single<VerificationStatus> {
    return walletService.getAndSignCurrentWalletAddress()
      .flatMap { addressModel ->
        brokerVerificationRepository.getServerVerificationState(
          addressModel.address,
          VerificationType.CREDIT_CARD
        )
      }
      .onErrorReturn { VerificationStatus.UNVERIFIED }
  }
}