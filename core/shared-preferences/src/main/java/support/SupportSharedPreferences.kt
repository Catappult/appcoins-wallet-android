package support

import android.content.SharedPreferences
import javax.inject.Inject

class SupportSharedPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences) {

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
