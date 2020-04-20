package com.asfoundation.wallet.support

import android.content.SharedPreferences

class SupportSharedPreferences(private val sharedPreferences: SharedPreferences) {

  fun checkSavedUnreadConversations(): Int =
      sharedPreferences.getInt(SupportNotificationWorker.UNREAD_CONVERSATIONS, 0)

  fun updateUnreadConversations(unreadConversations: Int) {
    sharedPreferences.edit()
        .apply {
          putInt(SupportNotificationWorker.UNREAD_CONVERSATIONS, unreadConversations)
          apply()
        }
  }

  fun resetUnreadConversations() {
    sharedPreferences.edit()
        .apply {
          putInt(SupportNotificationWorker.UNREAD_CONVERSATIONS, 0)
          apply()
        }
  }

}
