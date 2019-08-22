package com.asfoundation.wallet.referrals

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single

class SharedPreferencesReferralLocalData(private val preferences: SharedPreferences) :
    ReferralLocalData {

  override fun saveTotalEarned(address: String, totalEarned: String, screen: String): Completable {
    return Completable.fromCallable {
      preferences.edit()
          .putString(getKey(address,
              EARNED, screen), totalEarned)
          .apply()
    }
  }

  override fun saveNumberOfFriends(address: String, numberOfFriends: Int,
                                   screen: String): Completable {
    return Completable.fromCallable {
      preferences.edit()
          .putInt(getKey(address,
              FRIENDS, screen), numberOfFriends)
          .apply()
    }
  }

  override fun getNumberOfFriends(address: String, screen: String): Single<Int> {
    return Single.fromCallable {
      preferences.getInt(getKey(address,
          FRIENDS, screen), -1)
    }
  }

  override fun getEarned(address: String, screen: String): Single<String> {
    return Single.fromCallable {
      preferences.getString(getKey(address,
          EARNED, screen), "-1")
    }
  }

  private fun getKey(wallet: String, key: String, screen: String): String {
    return key + wallet + SCREEN + screen
  }

  companion object {
    private const val FRIENDS = "friends_"
    private const val EARNED = "earned_"
    private const val SCREEN = "screen_"
  }

}