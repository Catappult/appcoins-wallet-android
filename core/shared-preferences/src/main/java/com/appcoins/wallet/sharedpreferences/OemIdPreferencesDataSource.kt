package com.appcoins.wallet.sharedpreferences

import android.content.SharedPreferences
import javax.inject.Inject

class OemIdPreferencesDataSource @Inject constructor(
  private val sharedPreferences: SharedPreferences,
) {
  fun putOemIdCache(key: String?, value: String?) =
    sharedPreferences.edit()
      .putString(key, value)
      .apply()

  fun getOemIdCache(key: String?) = sharedPreferences.getString(key, "")

  fun setCurrentOemId(value: String?) =
    sharedPreferences.edit()
      .putString(CURRENT_OEMID, value)
      .apply()

  fun getGamesHubOemIdIndicative() =
    sharedPreferences.getString(GAMES_HUB_INSTALLED_OEMID, "") ?: ""

  fun setGamesHubOemIdIndicative(value: String?) =
    sharedPreferences.edit()
      .putString(GAMES_HUB_INSTALLED_OEMID, value)
      .apply()

  fun hasGamesHubOemId() =
    getGamesHubOemIdIndicative().isNotEmpty() &&
        getGamesHubOemIdIndicative() != GH_INSTALLED_WITHOUT_OEMID &&
        getGamesHubOemIdIndicative() != GH_NOT_INSTALLED

  fun getCurrentOemId() = sharedPreferences.getString(CURRENT_OEMID, "") ?: ""
  fun setIsGameFromGameshub(value: Boolean) =
    sharedPreferences.edit()
      .putBoolean(IS_GAME_FROM_GAMESHUB_KEY, value)
      .apply()

  fun getIsGameFromGameshub() = sharedPreferences.getBoolean(IS_GAME_FROM_GAMESHUB_KEY, false)

  fun setOemIdForPackage(packageName: String, oemid: String) =
    sharedPreferences.edit()
      .putString("${CLIENT_SIDE_CACHED_OEMID}${packageName}", oemid)
      .apply()

  fun getOemIdForPackage(packageName: String) =
    sharedPreferences.getString("${CLIENT_SIDE_CACHED_OEMID}${packageName}", "") ?: ""

  fun setPackageListClientSide(packages: List<String>) =
    sharedPreferences.edit()
      .putStringSet(PACKAGES_CLIENT_SIDE, packages.toSet())
      .apply()

  fun getPackageListClientSide() =
    sharedPreferences.getStringSet(PACKAGES_CLIENT_SIDE, setOf<String>())?.toList() ?: listOf()

  fun setLastTimePackagesForCaching(lastTime: Long) =
    sharedPreferences.edit()
      .putLong(LAST_TIMESTAMP_PACKAGES, lastTime)
      .apply()

  fun getLastTimePackagesForCaching() =
    sharedPreferences.getLong(LAST_TIMESTAMP_PACKAGES, 0L)

  fun setOemIdFromSdk(oemId: String?) =
    sharedPreferences.edit()
      .putString(OEM_ID_FROM_SDK, oemId ?: "")
      .apply()

  fun getOemIdFromSdk() = sharedPreferences.getString(OEM_ID_FROM_SDK, "") ?: ""

  companion object {
    private const val CURRENT_OEMID = "current_oemid"
    private const val IS_GAME_FROM_GAMESHUB_KEY = "game_from_gameshub"
    private const val CLIENT_SIDE_CACHED_OEMID = "client_side_cached_oemid"
    private const val PACKAGES_CLIENT_SIDE = "packages_client_side"
    private const val LAST_TIMESTAMP_PACKAGES = "last_timestamp_packages"
    private const val GAMES_HUB_INSTALLED_OEMID = "games_hub"
    const val GH_INSTALLED_WITHOUT_OEMID = "gh_installed_without_oemid"
    const val GH_NOT_INSTALLED = "gh_not_installed"
    private const val OEM_ID_FROM_SDK = "oem_id_from_sdk"
  }
}
