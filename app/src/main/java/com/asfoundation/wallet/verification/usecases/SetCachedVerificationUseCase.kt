package com.asfoundation.wallet.verification.usecases

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus
import io.reactivex.Completable
import javax.inject.Inject

class SetCachedVerificationUseCase @Inject constructor(private val walletService: WalletService,
                                                       private val brokerVerificationRepository: com.appcoins.wallet.feature.walletInfo.data.verification.BrokerVerificationRepository) {

  operator fun invoke(status: com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus): Completable {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMapCompletable { addressModel ->
          Completable.fromAction {
            brokerVerificationRepository.saveVerificationStatus(addressModel.address, status)
          }
        }
  }
}