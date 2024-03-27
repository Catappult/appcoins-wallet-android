package com.wallet.appcoins.feature.support.data

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
import com.appcoins.wallet.gamification.Gamification
import io.intercom.android.sdk.Intercom
import io.reactivex.Completable
import java.util.Locale
import javax.inject.Inject

class SupportInteractor @Inject constructor(
  private val supportRepository: SupportRepository,
  private val walletService: WalletService,
  private val gamificationRepository: Gamification,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase,
  private val rxSchedulers: RxSchedulers
) {

  fun showSupport(): Completable {
    return getCurrentPromoCodeUseCase()
      .flatMapCompletable { promoCode ->
        walletService.getWalletAddress()
          .flatMapCompletable { address ->
            gamificationRepository.getUserLevel(address, promoCode.code)
              .observeOn(rxSchedulers.main)
              .flatMapCompletable { showSupport(address, it) }
          }
          .subscribeOn(rxSchedulers.io)
      }
  }

  fun showSupport(gamificationLevel: Int): Completable {
    return walletService.getWalletAddress()
      .observeOn(rxSchedulers.main)
      .flatMapCompletable { showSupport(it, gamificationLevel) }
      .subscribeOn(rxSchedulers.io)
  }

  fun showSupport(walletAddress: String, gamificationLevel: Int): Completable {
    return Completable.fromAction {
      registerUser(gamificationLevel, walletAddress)
      displayChatScreen()
    }
  }

  fun displayChatScreen() {
    supportRepository.resetUnreadConversations()
    Intercom.client()
      .present()
  }

  fun registerUser(level: Int, walletAddress: String) {
    // force lowercase to make sure 2 users are not registered with the same wallet address, where
    // one has uppercase letters (to be check summed), and the other does not
    val address = walletAddress.lowercase(Locale.ROOT)
    val currentUser = supportRepository.getCurrentUser()
    if (currentUser.userAddress != address || currentUser.gamificationLevel != level) {
      if (currentUser.userAddress != address) {
        Intercom.client()
          .logout()
      }
      supportRepository.saveNewUser(address, level)
    }
  }

  fun hasNewUnreadConversations() =
    getUnreadConversations() > supportRepository.getSavedUnreadConversations()

  fun updateUnreadConversations() =
    supportRepository.updateUnreadConversations(Intercom.client().unreadConversationCount)

  private fun getUnreadConversations() = Intercom.client().unreadConversationCount
}