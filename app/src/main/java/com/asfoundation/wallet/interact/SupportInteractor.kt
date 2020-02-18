package com.asfoundation.wallet.interact

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
        .withCustomAttribute(USER_LEVEL_ATTRIBUTE, level)
        .build()
    val registration: Registration = Registration.create()
        .withUserId(walletAddress)
        .withUserAttributes(userAttributes)

    Intercom.client()
        .registerIdentifiedUser(registration)
    currentUser = walletAddress
  }

  fun getUnreadConversationCount(): Observable<Int> {
    return Observable.create<Int> {
      Intercom.client()
          .addUnreadConversationCountListener { i: Int -> it.onNext(i) }
    }
  }

  companion object {

    const val USER_LEVEL_ATTRIBUTE = "user_level"

  }

}