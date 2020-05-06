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

  fun displayChatScreen() {
    resetUnreadConversations()
    Intercom.client()
        .displayMessenger()
  }

  fun registerUser(level: Int, walletAddress: String) {
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
  }

  fun getUnreadConversationCountListener(): Observable<Int> {
    return Observable.create {
      Intercom.client()
          .addUnreadConversationCountListener { i: Int -> it.onNext(i) }
    }
  }

  fun getUnreadConversationCount() = Observable.just(Intercom.client().unreadConversationCount)

  fun shouldShowNotification() =
      getUnreadConversations() > preferences.checkSavedUnreadConversations()

  fun updateUnreadConversations() = preferences.updateUnreadConversations(getUnreadConversations())

  private fun resetUnreadConversations() = preferences.resetUnreadConversations()

  private fun getUnreadConversations() = Intercom.client().unreadConversationCount

}