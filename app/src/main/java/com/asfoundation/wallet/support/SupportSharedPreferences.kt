package com.asfoundation.wallet.support

import android.content.SharedPreferences

class SupportSharedPreferences(private val sharedPreferences: SharedPreferences) {

  companion object {
    private const val UNREAD_CONVERSATIONS = "UNREAD_CONVERSATIONS"
  }

  fun checkSavedUnreadConversations() = sharedPreferences.getInt(UNREAD_CONVERSATIONS, 0)

  fun updateUnreadConversations(unreadConversations: Int) {
    sharedPreferences.edit()
        .apply {
          putInt(UNREAD_CONVERSATIONS, unreadConversations)
          apply()
        }
  }

  fun resetUnreadConversations() {
    sharedPreferences.edit()
        .apply {
          putInt(UNREAD_CONVERSATIONS, 0)
          apply()
        }
  }

}
