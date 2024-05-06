package com.asfoundation.wallet.my_wallets.create_wallet

import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletCreatorInteract
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
  private val walletCreatorInteract: WalletCreatorInteract,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val supportInteractor: com.wallet.appcoins.feature.support.data.SupportInteractor
) {
  operator fun invoke(name: String?): Completable = walletCreatorInteract.create(name)
    .subscribeOn(Schedulers.io())
    .flatMapCompletable { wallet ->
      getCurrentPromoCodeUseCase()
        .flatMapCompletable { promoCode ->
          walletCreatorInteract.setDefaultWallet(wallet.address).toSingleDefault(promoCode)
            .doOnSuccess {
              supportInteractor.showSupport()
            }.subscribe()
          Completable.complete()
        }
    }
}