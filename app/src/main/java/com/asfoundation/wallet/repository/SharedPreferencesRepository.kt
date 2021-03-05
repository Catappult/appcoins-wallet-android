package com.asfoundation.wallet.repository

import android.content.SharedPreferences
import io.reactivex.Completable

/**
 * Repository that uses Shared Preferences to save some misc. information that doesn't fall in the
 * categories covered by other repositories that use Shared Preferences.
 *
 * @see ImpressionPreferencesRepository
 */
class SharedPreferencesRepository(private val pref: SharedPreferences) : PreferencesRepositoryType {

  companion object {

    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"
    private const val ANDROID_ID = "android_id"
    private const val WALLET_PURCHASES_COUNT = "wallet_purchases_count_"
    private const val WALLET_ID = "wallet_id"
  }

  override fun getCurrentWalletAddress(): String? {
    return pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null)
  }

  override fun setCurrentWalletAddress(address: String) {
    pref.edit()
        .putString(CURRENT_ACCOUNT_ADDRESS_KEY, address)
        .apply()
  }

  override fun getAndroidId() = pref.getString(ANDROID_ID, "")
      .orEmpty()


  override fun setAndroidId(androidId: String) {
    pref.edit()
        .putString(ANDROID_ID, androidId)
        .apply()
  }

  override fun getWalletPurchasesCount(walletAddress: String) =
      pref.getInt(WALLET_PURCHASES_COUNT + walletAddress, 0)

  override fun incrementWalletPurchasesCount(walletAddress: String, count: Int) =
      Completable.fromAction {
        pref.edit()
            .putInt(WALLET_PURCHASES_COUNT + walletAddress, count)
            .apply()
      }

  override fun setWalletId(walletId: String) {
    pref.edit()
        .putString(WALLET_ID, walletId)
        .apply()
  }

  override fun getWalletId() = pref.getString(WALLET_ID, null)
}
