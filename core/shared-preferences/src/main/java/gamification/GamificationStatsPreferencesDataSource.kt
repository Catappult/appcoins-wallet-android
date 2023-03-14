package gamification

import android.content.SharedPreferences
import javax.inject.Inject

class GamificationStatsPreferencesDataSource @Inject constructor(
  private val preferences: SharedPreferences
) {

  companion object {
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val SHOW_GAMIFICATION_DISCLAIMER = "SHOW_GAMIFICATION_DISCLAIMER"
    private const val WALLET_ORIGIN = "wallet_origin"
    private const val VIP_CALLOUT_SEEN = "vip_callout_seen"
    private const val REFERRAL_NOTIFIC_SEEN = "ref_notific_seen"
  }

  fun getLastShownLevel(key: String, gamificationStats: Int) =
    preferences.getInt(key, gamificationStats)

  fun saveShownLevel(key: String, level: Int) =
    preferences.edit()
      .putInt(key, level)
      .apply()

  fun getSeenGenericPromotion(genericKey: String) = preferences.getBoolean(genericKey, false)

  fun setSeenGenericPromotion(genericKey: String) =
    preferences.edit()
      .putBoolean(genericKey, true)
      .apply()


  fun setGamificationLevel(gamificationLevel: Int) =
    preferences.edit()
      .putInt(GAMIFICATION_LEVEL, gamificationLevel)
      .apply()

  fun getGamificationLevel(gamificationStats: Int) =
    preferences.getInt(GAMIFICATION_LEVEL, gamificationStats)

  fun shouldShowGamificationDisclaimer() =
    preferences.getBoolean(SHOW_GAMIFICATION_DISCLAIMER, true)

  fun setGamificationDisclaimerShown() =
    preferences.edit()
      .putBoolean(SHOW_GAMIFICATION_DISCLAIMER, false)
      .apply()

  fun setSeenWalletOrigin(wallet: String, walletOrigin: String) =
    preferences.edit()
      .putString(WALLET_ORIGIN + wallet, walletOrigin)
      .apply()

  fun getSeenWalletOrigin(wallet: String) = preferences.getString(WALLET_ORIGIN + wallet, "")!!

  fun isVipCalloutAlreadySeen(wallet: String) =
    preferences.getBoolean(VIP_CALLOUT_SEEN + wallet, false)

  fun setVipCalloutAlreadySeen(wallet: String, isSeen: Boolean) =
    preferences.edit()
      .putBoolean(VIP_CALLOUT_SEEN + wallet, isSeen)
      .apply()

  fun isReferralNotificationSeen(wallet: String) =
    preferences.getBoolean(REFERRAL_NOTIFIC_SEEN + wallet, false)

  fun setReferralNotificationSeen(wallet: String, isSeen: Boolean) =
    preferences.edit()
      .putBoolean(REFERRAL_NOTIFIC_SEEN + wallet, isSeen)
      .apply()
}