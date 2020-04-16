package com.asfoundation.wallet.ui.gamification

import android.content.SharedPreferences
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.GamificationLocalData
import io.reactivex.Completable
import io.reactivex.Single

class SharedPreferencesGamificationLocalData(private val preferences: SharedPreferences) :
    GamificationLocalData {

  companion object {
    private const val SHOWN_LEVEL = "shown_level"
    private const val SCREEN = "screen_"
    private const val GAMIFICATION_LEVEL = "gamification_level"
  }

  override fun getLastShownLevel(wallet: String, screen: String): Single<Int> {
    return Single.fromCallable { preferences.getInt(getKey(wallet, screen), -1) }
  }

  override fun saveShownLevel(wallet: String, level: Int, screen: String): Completable {
    return Completable.fromCallable {
      preferences.edit()
          .putInt(getKey(wallet, screen), level)
          .apply()
    }
  }

  override fun setGamificationLevel(gamificationLevel: Int): Completable {
    return Completable.fromCallable {
      preferences.edit()
          .putInt(GAMIFICATION_LEVEL, gamificationLevel)
          .apply()
    }
  }


  private fun getKey(wallet: String, screen: String): String {
    return if (screen == GamificationScreen.MY_LEVEL.toString()) {
      SHOWN_LEVEL + wallet
    } else {
      SHOWN_LEVEL + wallet + SCREEN + screen
    }
  }
}