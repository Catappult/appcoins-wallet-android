package referrals

import android.content.SharedPreferences
import javax.inject.Inject

class ReferralPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {

  fun saveReferralInformation(
    address: String, invitedFriends: Int, isVerified: Boolean, screen: String
  ) =
    sharedPreferences.edit()
      .putString(getKey(address, screen), invitedFriends.toString() + isVerified)
      .apply()

  fun getReferralInformation(address: String, screen: String) =
    sharedPreferences.getString(getKey(address, screen), "-1")

  fun savePendingAmountNotification(address: String, pendingAmount: String) =
    sharedPreferences.edit()
      .putString(getKey(address), pendingAmount)
      .apply()

  fun getPendingAmountNotification(address: String) =
    sharedPreferences.getString(getKey(address), "0")

  private fun getKey(wallet: String, screen: String) = CONTEXT + wallet + SCREEN + screen

  private fun getKey(wallet: String) = CONTEXT + wallet + PENDING_AMOUNT

  companion object {
    private const val CONTEXT = "REFERRALS"
    private const val SCREEN = "screen_"
    private const val PENDING_AMOUNT = "referrals_pending_amount"
  }

}