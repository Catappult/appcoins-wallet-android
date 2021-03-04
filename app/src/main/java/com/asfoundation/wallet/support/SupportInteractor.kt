package com.asfoundation.wallet.support

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.gamification.Gamification
import io.intercom.android.sdk.Intercom
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.*

class SupportInteractor(private val supportRepository: SupportRepository,
                        private val walletService: WalletService,
                        private val gamificationRepository: Gamification,
                        private val viewScheduler: Scheduler,
                        private val ioScheduler: Scheduler) {

  fun showSupport(): Completable {
    return walletService.getWalletAddress()
        .flatMapCompletable { address ->
          gamificationRepository.getUserStats(address)
              .firstOrError()
              .observeOn(viewScheduler)
              .flatMapCompletable { gamificationStats ->
                showSupport(address, gamificationStats.level)
              }
        }
        .subscribeOn(ioScheduler)
  }

  fun showSupport(gamificationLevel: Int): Completable {
    return walletService.getWalletAddress()
        .observeOn(viewScheduler)
        .flatMapCompletable { address ->
          showSupport(address, gamificationLevel)
        }
        .subscribeOn(ioScheduler)
  }

  fun showSupport(walletAddress: String, gamificationLevel: Int): Completable {
    return Completable.fromAction {
      registerUser(gamificationLevel, walletAddress.toLowerCase(Locale.ROOT))
      displayChatScreen()
    }
  }

  fun displayChatScreen() {
    supportRepository.resetUnreadConversations()
    Intercom.client()
        .displayMessenger()
  }

  @Suppress("DEPRECATION")
  fun displayConversationListOrChat() {
    //this method was introduced because if the app is closed intercom returns 0 unread conversations
    //even if there are more
    supportRepository.resetUnreadConversations()
    val handledByIntercom = getUnreadConversations() > 0
    if (handledByIntercom) {
      Intercom.client()
          .displayMessenger()
    } else {
      Intercom.client()
          .displayConversationsList()
    }
  }

  fun registerUser(level: Int, walletAddress: String) {
    val currentUser = supportRepository.getCurrentUser()
    if (currentUser.userAddress != walletAddress || (currentUser.gamificationLevel != level &&
            !levelBeingInvalidated(level, currentUser.gamificationLevel))) {
      if (currentUser.userAddress != walletAddress) {
        Intercom.client()
            .logout()
      }
      supportRepository.saveNewUser(walletAddress, level)
    }
  }

  fun hasNewUnreadConversations() =
      getUnreadConversations() > supportRepository.getSavedUnreadConversations()

  fun updateUnreadConversations() =
      supportRepository.updateUnreadConversations(Intercom.client().unreadConversationCount)

  fun getUnreadConversationCountEvents() = Observable.create<Int> {
    Intercom.client()
        .addUnreadConversationCountListener { unreadCount -> it.onNext(unreadCount) }
  }

  fun getUnreadConversationCount() = Observable.just(Intercom.client().unreadConversationCount)

  private fun getUnreadConversations() = Intercom.client().unreadConversationCount

  private fun levelBeingInvalidated(newLevel: Int, currentUserLevel: Int): Boolean {
    // in case of error for same user address (newLevel=-1 and currentLevel>=0),
    // user info isn't left in inconsistent state
    return (newLevel < 0 && currentUserLevel > newLevel)
  }
}