package repository

import android.content.SharedPreferences
import javax.inject.Inject

class BackupRestorePreferencesRepository @Inject constructor(private val pref: SharedPreferences) {

  companion object {
    private const val KEYSTORE_DIRECTORY = "keystore_directory"
  }

  fun saveChosenUri(uri: String) {
    pref.edit()
      .putString(KEYSTORE_DIRECTORY, uri)
      .apply()
  }

  fun getChosenUri() = pref.getString(KEYSTORE_DIRECTORY, null)
}
