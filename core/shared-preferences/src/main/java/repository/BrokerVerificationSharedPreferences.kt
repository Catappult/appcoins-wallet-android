package repository

import android.content.SharedPreferences
import javax.inject.Inject

class BrokerVerificationSharedPreferences @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun saveVerificationStatus(walletAddress: String, verificationStatusId: Int) {
    sharedPreferences.edit()
      .putInt(WALLET_VERIFIED + walletAddress, verificationStatusId)
      .apply()
  }

  fun getCachedValidationStatus(walletAddress: String) =
    sharedPreferences.getInt(WALLET_VERIFIED + walletAddress, 4)

  fun removeCachedWalletValidationStatus(walletAddress: String) {
    sharedPreferences.edit()
      .remove(WALLET_VERIFIED + walletAddress)
      .apply()
  }

  companion object {
    private const val WALLET_VERIFIED = "wallet_verified_cc_"
  }
}