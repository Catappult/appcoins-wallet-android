package repository

import android.content.SharedPreferences
import javax.inject.Inject

class BackupRestorePreferencesDataSource @Inject constructor(private val sharedPreferences: SharedPreferences) {
  fun saveChosenUri(uri: String) =
    sharedPreferences.edit()
      .putString(KEYSTORE_DIRECTORY, uri)
      .apply()

  fun getChosenUri() = sharedPreferences.getString(KEYSTORE_DIRECTORY, null)

  companion object {
    private const val KEYSTORE_DIRECTORY = "keystore_directory"
  }
}
