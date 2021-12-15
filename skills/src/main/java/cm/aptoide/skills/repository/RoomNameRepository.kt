package cm.aptoide.skills.repository

import android.content.SharedPreferences

class RoomNameRepository(private val preferences: SharedPreferences) {
  companion object {
    const val ROOM_NAME_KEY = "ROOM_NAME_KEY"
  }

  fun saveRoomName(roomName: String) {
    val editPreferences = preferences.edit()
    editPreferences.putString(ROOM_NAME_KEY, roomName)
    editPreferences.apply()
  }

  fun getRoomName(): String? {
    return preferences.getString(ROOM_NAME_KEY, null)
  }
}
