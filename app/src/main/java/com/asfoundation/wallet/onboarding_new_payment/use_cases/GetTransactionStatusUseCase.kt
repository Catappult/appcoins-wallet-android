package com.asfoundation.wallet.onboarding_new_payment.use_cases

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.core.network.microservices.model.Transaction
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Single
import javax.inject.Inject

class GetTransactionStatusUseCase @Inject constructor(
    private val walletService: WalletService,
    private val remoteRepository: RemoteRepository,
    private val rxSchedulers: RxSchedulers
) {
    operator fun invoke(uid: String): Single<Transaction> {
        return walletService.getAndSignCurrentWalletAddress().subscribeOn(rxSchedulers.io)
            .flatMap {
                remoteRepository.getAppcoinsTransaction(
                    uid,
                    it.address,
                    it.signedAddress
                )
            }
    }
}


