package com.asfoundation.wallet.interact

import io.intercom.android.sdk.Intercom
import io.intercom.android.sdk.UserAttributes
import io.intercom.android.sdk.identity.Registration

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
        .withCustomAttribute("user_level", level)
        .build()
    val registration: Registration = Registration.create()
        .withUserId(walletAddress)
        .withUserAttributes(userAttributes)

    Intercom.client()
        .registerIdentifiedUser(registration)
    currentUser = walletAddress
  }

  fun logoutUser() {
    Intercom.client()
        .logout()
  }
}