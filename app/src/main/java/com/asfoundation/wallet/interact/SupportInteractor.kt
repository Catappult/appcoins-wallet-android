package com.asfoundation.wallet.interact

import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.identity.Registration

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

  fun logoutUser() {
    Intercom.client()
        .logout()
  }
}