package cm.aptoide.skills.usecase

import android.util.Log
import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.TopUpStatus
import cm.aptoide.skills.model.TransactionType
import cm.aptoide.skills.repository.TopUpRepository
import io.reactivex.Single
import javax.inject.Inject

class IsTopUpListEmptyUseCase @Inject constructor(private val topUpRepository: TopUpRepository, private val walletAddressObtainer: WalletAddressObtainer) {
    operator fun invoke(type: TransactionType, status: TopUpStatus): Single<Boolean> {
        return  walletAddressObtainer.getWalletAddress()
                .flatMap { wallet ->
                    topUpRepository.getTopUpHistory(type, status, wallet.address).map {
                        it.items?.isEmpty() ?: true
                    }
                }//.doOnError { Log.e("Error:", "invoke: ${it.stackTraceToString()}") }
    }
}