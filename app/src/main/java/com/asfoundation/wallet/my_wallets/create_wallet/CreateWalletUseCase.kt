package com.asfoundation.wallet.my_wallets.create_wallet

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.wallets.WalletCreatorInteract
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CreateWalletUseCase @Inject constructor(
  private val walletCreatorInteract: WalletCreatorInteract,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val gamification: Gamification,
  private val supportInteractor: SupportInteractor
) {
  operator fun invoke(name: String?): Completable = walletCreatorInteract.create(name)
    .subscribeOn(Schedulers.io())
    .flatMapCompletable { wallet ->
      getCurrentPromoCodeUseCase()
        .flatMapCompletable { promoCode ->
          walletCreatorInteract.setDefaultWallet(wallet.address).toSingleDefault(promoCode)
            .doOnSuccess {
              gamification.getUserLevel(wallet.address, promoCode.code)
                .map { level ->
                  supportInteractor.registerUser(level, wallet.address)
                }
            }.subscribe()
          Completable.complete()
        }
    }
}