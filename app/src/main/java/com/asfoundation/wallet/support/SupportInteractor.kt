package com.asfoundation.wallet.support

import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.UserAttributes
import io.intercom.android.sdk.identity.Registration
import io.reactivex.Observable

class SupportInteractor {

  private var currentUser = ""

  fun displayChatScreen() {
    Intercom.client()
        .displayMessenger()
  }

  fun registerUser(level: Int, walletAddress: String) {
    if (currentUser != walletAddress) {
      Intercom.client()
          .logout()
    }

    val userAttributes = UserAttributes.Builder()
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
    return Observable.create<Int> {
      Intercom.client()
          .addUnreadConversationCountListener { i: Int -> it.onNext(i) }
    }
  }

  fun getUnreadConversationCount(): Observable<Int> {
    return Observable.just(Intercom.client().unreadConversationCount)
  }

  fun shouldShowNotification(): Boolean {
    /*return (Intercom.client()
        .unreadConversationCount != 0)*/
    return true
  }

  companion object {
    const val USER_LEVEL_ATTRIBUTE = "user_level"
  }

}