package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class SupportPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {

  companion object {
    private const val UNREAD_CONVERSATIONS = "UNREAD_CONVERSATIONS"
  }

  fun checkSavedUnreadConversations() = sharedPreferences.getInt(UNREAD_CONVERSATIONS, 0)

  fun updateUnreadConversations(unreadConversations: Int) =
    sharedPreferences.edit()
      .putInt(UNREAD_CONVERSATIONS, unreadConversations)
      .apply()

  fun resetUnreadConversations() =
    sharedPreferences.edit()
      .putInt(UNREAD_CONVERSATIONS, 0)
      .apply()
}
