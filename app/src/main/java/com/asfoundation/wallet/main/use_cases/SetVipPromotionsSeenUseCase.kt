package com.asfoundation.wallet.main.use_cases

import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asfoundation.wallet.home.usecases.ObserveDefaultWalletUseCase
import io.reactivex.Completable
import io.reactivex.internal.operators.completable.CompletableFromAction
import javax.inject.Inject

class SetVipPromotionsSeenUseCase @Inject constructor(
  private val promotionsRepository: PromotionsRepository,
  private val observeDefaultWalletUseCase: ObserveDefaultWalletUseCase
) {

  operator fun invoke(isSeen: Boolean): Completable {
    return observeDefaultWalletUseCase()
      .flatMapCompletable {
        CompletableFromAction {
          promotionsRepository.setVipCalloutAlreadySeen(it.address, isSeen)
        }
      }
  }
}
