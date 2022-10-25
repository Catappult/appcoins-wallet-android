package cm.aptoide.skills.usecase

import cm.aptoide.skills.interfaces.WalletAddressObtainer
import cm.aptoide.skills.model.Gateway
import cm.aptoide.skills.model.TopUpStatus
import cm.aptoide.skills.model.TransactionType
import cm.aptoide.skills.repository.TopUpRepository
import io.reactivex.Single
import javax.inject.Inject

class IsTopUpListEmptyUseCase @Inject constructor(
  private val topUpRepository: TopUpRepository,
  private val walletAddressObtainer: WalletAddressObtainer
) {
  operator fun invoke(type: TransactionType, status: TopUpStatus): Single<Status> {
    return walletAddressObtainer.getWalletAddress()
      .flatMap { wallet ->
        topUpRepository.getTopUpHistory(type, status, wallet.address)
          .toObservable()
          .flatMapIterable { it.items }
          .filter { it.gateway?.name != Gateway.Name.myappcoins }
          .toList().map { transaction ->
            if(transaction.isEmpty()){
              Status.NO_TOPUP
            }
            if (transaction.any{it.gateway?.name == Gateway.Name.myappcoins} ){
              Status.PAYMENT_METHOD_NOT_SUPPORTED
            }
            Status.AVAILABLE
          }
      }
  }
}
enum class Status{
  PAYMENT_METHOD_NOT_SUPPORTED, NO_TOPUP, AVAILABLE
}