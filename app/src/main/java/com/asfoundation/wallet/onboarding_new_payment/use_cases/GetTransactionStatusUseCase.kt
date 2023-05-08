package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.core.network.microservices.model.Transaction
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class GetTransactionStatusUseCase @Inject constructor(
    private val walletService: WalletService,
    private val remoteRepository: RemoteRepository
) {
    operator fun invoke(uid: String): Single<Transaction> {
        return walletService.getAndSignCurrentWalletAddress().subscribeOn(Schedulers.io())
            .flatMap {
                remoteRepository.getAppcoinsTransaction(
                    uid,
                    it.address,
                    it.signedAddress
                )
            }
    }
}


