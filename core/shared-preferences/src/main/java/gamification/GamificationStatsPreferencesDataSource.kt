package gamification

import android.content.SharedPreferences
import javax.inject.Inject

class GamificationStatsPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences
) {

  companion object {
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val SHOW_GAMIFICATION_DISCLAIMER = "SHOW_GAMIFICATION_DISCLAIMER"
    private const val WALLET_ORIGIN = "wallet_origin"
    private const val VIP_CALLOUT_SEEN = "vip_callout_seen"
    private const val REFERRAL_NOTIFIC_SEEN = "ref_notific_seen"
  }

  fun getLastShownLevel(key: String, gamificationStats: Int) =
    sharedPreferences.getInt(key, gamificationStats)

  fun saveShownLevel(key: String, level: Int) =
    sharedPreferences.edit()
      .putInt(key, level)
      .apply()

  fun getSeenGenericPromotion(genericKey: String) = sharedPreferences.getBoolean(genericKey, false)

  fun setSeenGenericPromotion(genericKey: String) =
    sharedPreferences.edit()
      .putBoolean(genericKey, true)
      .apply()


  fun setGamificationLevel(gamificationLevel: Int) =
    sharedPreferences.edit()
      .putInt(GAMIFICATION_LEVEL, gamificationLevel)
      .apply()

  fun getGamificationLevel(gamificationStats: Int) =
    sharedPreferences.getInt(GAMIFICATION_LEVEL, gamificationStats)

  fun shouldShowGamificationDisclaimer() =
    sharedPreferences.getBoolean(SHOW_GAMIFICATION_DISCLAIMER, true)

  fun setGamificationDisclaimerShown() =
    sharedPreferences.edit()
      .putBoolean(SHOW_GAMIFICATION_DISCLAIMER, false)
      .apply()

  fun setSeenWalletOrigin(wallet: String, walletOrigin: String) =
    sharedPreferences.edit()
      .putString(WALLET_ORIGIN + wallet, walletOrigin)
      .apply()

  fun getSeenWalletOrigin(wallet: String) =
    sharedPreferences.getString(WALLET_ORIGIN + wallet, "")!!

  fun isVipCalloutAlreadySeen(wallet: String) =
    sharedPreferences.getBoolean(VIP_CALLOUT_SEEN + wallet, false)

  fun setVipCalloutAlreadySeen(wallet: String, isSeen: Boolean) =
    sharedPreferences.edit()
      .putBoolean(VIP_CALLOUT_SEEN + wallet, isSeen)
      .apply()

  fun isReferralNotificationSeen(wallet: String) =
    sharedPreferences.getBoolean(REFERRAL_NOTIFIC_SEEN + wallet, false)

  fun setReferralNotificationSeen(wallet: String, isSeen: Boolean) =
    sharedPreferences.edit()
      .putBoolean(REFERRAL_NOTIFIC_SEEN + wallet, isSeen)
      .apply()
}