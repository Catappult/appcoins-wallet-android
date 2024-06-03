package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class BrokerVerificationPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {
  fun saveVerificationStatus(
    walletAddress: String,
    verificationStatusId: Int,
    typeOfVerification: Int
  ) =
    sharedPreferences.edit()
      .putInt(
        WALLET_VERIFIED + walletAddress + WALLET_TYPE_VERIFIED + typeOfVerification,
        verificationStatusId
      )
      .apply()

  fun getCachedValidationStatus(walletAddress: String, typeOfVerification: Int) =
    sharedPreferences.getInt(
      WALLET_VERIFIED + walletAddress + WALLET_TYPE_VERIFIED + typeOfVerification,
      4
    )

  fun removeCachedWalletValidationStatus(walletAddress: String, typeOfVerification: Int) =
    sharedPreferences.edit()
      .remove(WALLET_VERIFIED + walletAddress + WALLET_TYPE_VERIFIED + typeOfVerification)
      .apply()

  companion object {
    private const val WALLET_VERIFIED = "wallet_verified_cc_"
    private const val WALLET_TYPE_VERIFIED = "wallet_type_verified"
  }
}