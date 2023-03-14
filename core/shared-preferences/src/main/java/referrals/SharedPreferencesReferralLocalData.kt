package referrals

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = ReferralLocalData::class)
class SharedPreferencesReferralLocalData @Inject constructor(
    private val preferences: SharedPreferences) : ReferralLocalData {

  override fun saveReferralInformation(address: String, invitedFriends: Int,
                                       isVerified: Boolean, screen: String): Completable {
    return Completable.fromCallable {
      preferences.edit()
          .putString(getKey(address, screen), invitedFriends.toString() + isVerified)
          .apply()
    }
  }

  override fun getReferralInformation(address: String, screen: String): Single<String> {
    return Single.fromCallable {
      preferences.getString(getKey(address, screen), "-1")
    }
  }

  override fun savePendingAmountNotification(address: String, pendingAmount: String): Completable {
    return Completable.fromCallable {
      preferences.edit()
          .putString(getKey(address), pendingAmount)
          .apply()
    }
  }

  override fun getPendingAmountNotification(address: String): Single<String> {
    return Single.fromCallable {
      preferences.getString(getKey(address), "0")
    }
  }

  private fun getKey(wallet: String, screen: String): String {
    return CONTEXT + wallet + SCREEN + screen
  }

  private fun getKey(wallet: String): String {
    return CONTEXT + wallet + PENDING_AMOUNT
  }

  companion object {
    private const val CONTEXT = "REFERRALS"
    private const val SCREEN = "screen_"
    private const val PENDING_AMOUNT = "referrals_pending_amount"
  }

}