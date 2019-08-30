package com.asfoundation.wallet.referrals

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single

class SharedPreferencesReferralLocalData(private val preferences: SharedPreferences) :
    ReferralLocalData {

  override fun saveReferralInformation(address: String, totalEarned: String, invitedFriends: Int,
                                       isVerified: Boolean, screen: String): Completable {
    return Completable.fromCallable {
      preferences.edit()
          .putString(getKey(address, screen), totalEarned + invitedFriends + isVerified)
          .apply()
    }
  }

  override fun getReferralInformation(address: String, screen: String): Single<String> {
    return Single.fromCallable {
      preferences.getString(getKey(address, screen), "-1")
    }
  }

  private fun getKey(wallet: String, screen: String): String {
    return CONTEXT + wallet + SCREEN + screen
  }

  companion object {
    private const val CONTEXT = "REFERRALS"
    private const val SCREEN = "screen_"
  }

}