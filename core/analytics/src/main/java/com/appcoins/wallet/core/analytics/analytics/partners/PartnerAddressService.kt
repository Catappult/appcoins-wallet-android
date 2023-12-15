package com.appcoins.wallet.core.analytics.analytics.partners

import android.util.Log
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.appcoins.wallet.sharedpreferences.OemIdPreferencesDataSource
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = AddressService::class)
class PartnerAddressService @Inject constructor(
  private val installerService: InstallerService,
  private val oemIdExtractorService: OemIdExtractorService,
  private val oemIdPreferencesDataSource: OemIdPreferencesDataSource
) :
  AddressService {

  val DEFAULT_STORE_ADDRESS = "0xc41b4160b63d1f9488937f7b66640d2babdbf8ad"
  val DEFAULT_OEM_ADDRESS = "DEFAULT_OEM_ADDRESS"

  private val defaultStoreAddress: String = DEFAULT_STORE_ADDRESS
  private val defaultOemAddress: String = DEFAULT_OEM_ADDRESS
  private val defaultGamesHubPackage: String = MiscProperties.GAMESHUB_PACKAGE

  override fun getStoreAddress(suggestedStoreAddress: String?): String {
    return suggestedStoreAddress?.let { suggestedStoreAddress } ?: defaultStoreAddress
  }

  override fun getOemAddress(suggestedOemAddress: String?): String {
    return suggestedOemAddress?.let { suggestedOemAddress } ?: defaultOemAddress
  }

  override fun getAttribution(packageName: String): Single<AttributionEntity> {
    return getAttributionClientCache(packageName)
      .doOnSuccess {
        Log.d("oemid", "oemid: ${it?.oemId ?: ""}")
        oemIdPreferencesDataSource.setCurrentOemId(it.oemId ?: "")
      }
  }

  override fun getAttributionClientCache(packageName: String): Single<AttributionEntity> {
    return Single.zip(
      installerService.getInstallerPackageName(packageName),
      oemIdExtractorService.extractOemId(packageName)
    ) { installerPackage, oemId ->
      AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
    }
      .flatMap { attributionFromGame ->
        oemIdPreferencesDataSource.setIsGameFromGameshub(
          attributionFromGame.oemId == MiscProperties.GAME_FROM_GAMESHUB_OEMID
        )
        // Tries to send gamesHub's oemid, if available. Otherwise sends the oemid of the game.
        oemIdExtractorService.extractOemId(defaultGamesHubPackage)
          .map { gamesHubOemId ->
            if (gamesHubOemId.isEmpty()) {
              attributionFromGame
            } else {
              AttributionEntity(gamesHubOemId.ifEmpty { null }, attributionFromGame.domain)
            }
          }
      }
      .map { attribution ->
        // if there is an oemId in the cache, use it instead of the one extracted from the game.
        val oemIdFromCache = oemIdPreferencesDataSource.getOemIdForPackage(packageName)
        if (oemIdFromCache.isBlank()) {
          // save the oemId extracted from the game in the cache.
          oemIdPreferencesDataSource.setOemIdForPackage(packageName, attribution.oemId ?: "")
          attribution
        } else {
          AttributionEntity(oemIdFromCache, attribution.domain)
        }
      }
  }

  fun isGameFromGamesHub(): Boolean {
    return oemIdPreferencesDataSource.getIsGameFromGameshub()
  }
}
