package com.wallet.appcoins.feature.support.data

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.gamification.Gamification
import io.reactivex.Completable
import javax.inject.Inject

class SupportInteractor @Inject constructor(
  private val supportRepository: SupportRepository,
  private val walletService: WalletService,
  private val gamificationRepository: Gamification,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val rxSchedulers: RxSchedulers
) {

  fun showSupport(uid: String? = null): Completable {
    return getCurrentPromoCodeUseCase()
      .flatMapCompletable { promoCode ->
        walletService.getWalletAddress()
          .flatMapCompletable { address ->
            gamificationRepository.getUserLevel(address, promoCode.code)
              .observeOn(rxSchedulers.main)
              .flatMapCompletable { openIntercom(address, it, uid) }
          }
          .subscribeOn(rxSchedulers.io)
      }
  }

  fun showSupport(gamificationLevel: Int, uid: String? = null): Completable {
    return walletService.getWalletAddress()
      .observeOn(rxSchedulers.main)
      .flatMapCompletable { openIntercom(it, gamificationLevel, uid) }
      .subscribeOn(rxSchedulers.io)
  }

  private fun openIntercom(
    walletAddress: String,
    gamificationLevel: Int,
    uid: String?
  ): Completable {
    return Completable.fromAction {
      supportRepository.registerUser(gamificationLevel, walletAddress)
      supportRepository.openIntercom(uid)
    }
  }

  fun hasNewUnreadConversations() = supportRepository.hasUnreadConversations()
}