package com.asfoundation.wallet.verification.usecases

import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus
import io.reactivex.Completable
import javax.inject.Inject

class SetCachedVerificationUseCase @Inject constructor(private val walletService: WalletService,
                                                       private val brokerVerificationRepository: BrokerVerificationRepository) {

  operator fun invoke(status: VerificationStatus): Completable {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMapCompletable { addressModel ->
          Completable.fromAction {
            brokerVerificationRepository.saveVerificationStatus(addressModel.address, status)
          }
        }
  }
}