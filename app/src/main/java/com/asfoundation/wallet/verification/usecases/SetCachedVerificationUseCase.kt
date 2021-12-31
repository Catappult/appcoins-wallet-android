package com.asfoundation.wallet.verification.usecases

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.verification.repository.VerificationRepository
import com.asfoundation.wallet.verification.ui.credit_card.network.VerificationStatus
import io.reactivex.Completable
import javax.inject.Inject

class SetCachedVerificationUseCase @Inject constructor(private val walletService: WalletService,
                                                       private val verificationRepository: VerificationRepository) {

  operator fun invoke(status: VerificationStatus): Completable {
    return walletService.getAndSignCurrentWalletAddress()
        .flatMapCompletable { addressModel ->
          Completable.fromAction {
            verificationRepository.saveVerificationStatus(addressModel.address, status)
          }
        }
  }
}