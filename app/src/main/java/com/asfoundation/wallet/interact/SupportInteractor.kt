package com.asfoundation.wallet.interact

import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.identity.Registration
import io.reactivex.Observable

class SupportInteractor {

  private var currentUser = ""

  fun displayChatScreen() {
    Intercom.client()
        .displayMessenger()
  }

  fun registerUser(walletAddress: String) {
    if (currentUser != walletAddress) {
      Intercom.client()
          .logout()
    }
    Intercom.client()
        .registerIdentifiedUser(Registration.create().withUserId(walletAddress))
    currentUser = walletAddress
  }

  fun getUnreadConversationCount(): Observable<Int> {
    return Observable.create<Int> {
      Intercom.client()
          .addUnreadConversationCountListener { i: Int -> it.onNext(i) }
    }
  }

}