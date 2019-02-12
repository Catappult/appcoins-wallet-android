package com.asfoundation.wallet.ui.gamification

import android.content.SharedPreferences
import com.appcoins.wallet.gamification.repository.GamificationLocalData
import io.reactivex.Single

class SharedPreferencesGamificationLocalData(private val preferences: SharedPreferences) :
    GamificationLocalData {
  companion object {
    private const val SHOWN_LEVEL = "shown_level"
  }

  override fun getLastShownLevel(wallet: String): Single<Int> {
    return Single.fromCallable { preferences.getInt(SHOWN_LEVEL + wallet, -1) }
  }

}