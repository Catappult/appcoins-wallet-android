package repository

import android.content.SharedPreferences
import javax.inject.Inject

class AppStartPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun getRunCount(): Int = sharedPreferences.getInt(RUNS_COUNT, 0)

  fun saveRunCount(count: Int) = sharedPreferences.edit()
    .putInt(RUNS_COUNT, count)
    .apply()

  companion object {
    internal const val RUNS_COUNT = "AppStartRepository.RunsCount"
  }
}