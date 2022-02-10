package cm.aptoide.skills.usecase

import cm.aptoide.skills.model.CachedPayment
import cm.aptoide.skills.model.WalletAddress
import cm.aptoide.skills.repository.PaymentLocalStorage
import io.reactivex.Single
import javax.inject.Inject

class GetCachedPaymentUseCase @Inject constructor(private val paymentLocalStorage: PaymentLocalStorage) {
  operator fun invoke(walletAddress: WalletAddress): Single<CachedPayment> {
    return paymentLocalStorage.get(walletAddress)
  }
}
