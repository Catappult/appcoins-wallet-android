package com.asfoundation.wallet.verification.ui.credit_card

import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus
import io.reactivex.Single
import javax.inject.Inject

class VerificationCreditCardActivityInteractor @Inject constructor(
    private val brokerVerificationRepository: BrokerVerificationRepository,
    private val walletService: WalletService
) {

  fun getVerificationStatus(): Single<VerificationStatus> {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMap { addressModel ->
          brokerVerificationRepository.getCardVerificationState(addressModel.address,
              addressModel.signedAddress)
        }
        .onErrorReturn { VerificationStatus.UNVERIFIED }
  }
}