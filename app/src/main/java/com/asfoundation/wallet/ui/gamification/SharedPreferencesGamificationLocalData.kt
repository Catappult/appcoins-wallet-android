package com.asfoundation.wallet.ui.gamification

import android.content.SharedPreferences
import com.appcoins.wallet.gamification.repository.GamificationLocalData
import io.reactivex.Completable
import io.reactivex.Single

class SharedPreferencesGamificationLocalData(private val preferences: SharedPreferences) :
    GamificationLocalData {
  companion object {
    private const val SHOWN_LEVEL = "shown_level"
  }

  override fun getLastShownLevel(wallet: String): Single<Int> {
    return Single.fromCallable { preferences.getInt(getKey(wallet), -1) }
  }

  override fun saveShownLevel(wallet: String, level: Int): Completable {
    return Completable.fromCallable { preferences.edit().putInt(getKey(wallet), level).apply() }
  }

  private fun getKey(wallet: String) = SHOWN_LEVEL + wallet

}