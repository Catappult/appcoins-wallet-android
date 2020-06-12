package com.asfoundation.wallet.support

import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.UserAttributes
import io.intercom.android.sdk.identity.Registration
import io.reactivex.Observable

class SupportInteractor(private val preferences: SupportSharedPreferences) {

  companion object {
    private const val USER_LEVEL_ATTRIBUTE = "user_level"
  }

  private var currentUser = ""
  private var currentGamificationLevel = -1

  fun displayChatScreen() {
    resetUnreadConversations()
    Intercom.client()
        .displayMessenger()
  }

  fun registerUser(level: Int, walletAddress: String) {
    if (currentUser != walletAddress || currentGamificationLevel != level) {
      if (currentUser != walletAddress) {
        Intercom.client()
            .logout()
      }

      val userAttributes = UserAttributes.Builder()
          .withName(walletAddress)
          .withCustomAttribute(USER_LEVEL_ATTRIBUTE,
              level + 1)//we set level + 1 to help with readability for the support team
          .build()
      val registration: Registration = Registration.create()
          .withUserId(walletAddress)
          .withUserAttributes(userAttributes)

      Intercom.client()
          .registerIdentifiedUser(registration)
      currentUser = walletAddress
      currentGamificationLevel = level
    }
  }

  fun getUnreadConversationCountListener() = Observable.create<Int> {
    Intercom.client()
        .addUnreadConversationCountListener { unreadCount -> it.onNext(unreadCount) }
  }

  fun getUnreadConversationCount() = Observable.just(Intercom.client().unreadConversationCount)

  fun shouldShowNotification() =
      getUnreadConversations() > preferences.checkSavedUnreadConversations()

  fun updateUnreadConversations() = preferences.updateUnreadConversations(getUnreadConversations())

  private fun resetUnreadConversations() = preferences.resetUnreadConversations()

  private fun getUnreadConversations() = Intercom.client().unreadConversationCount

}