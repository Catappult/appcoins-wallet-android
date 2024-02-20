package com.appcoins.wallet.core.analytics.analytics.partners

import android.util.Log
import com.appcoins.wallet.core.network.backend.api.PartnerAttributionApi
import com.appcoins.wallet.core.utils.properties.MiscProperties
import com.appcoins.wallet.sharedpreferences.OemIdPreferencesDataSource
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = AddressService::class)
class PartnerAddressService
@Inject
constructor(
    private val installerService: InstallerService,
    private val oemIdExtractorService: OemIdExtractorService,
    private val oemIdPreferencesDataSource: OemIdPreferencesDataSource,
    private val partnerAttributionApi: PartnerAttributionApi
) : AddressService {

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
    return getAttributionClientCache(packageName).doOnSuccess {
      Log.d("oemid", "oemid: ${it?.oemId ?: ""}")
      oemIdPreferencesDataSource.setCurrentOemId(it.oemId ?: "")
    }
  }

  override fun getAttributionClientCache(packageName: String): Single<AttributionEntity> {
    return Single.zip(
            installerService.getInstallerPackageName(packageName),
            oemIdExtractorService.extractOemId(packageName)) { installerPackage, oemId ->
              AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
            }
        .flatMap { attributionFromGame ->
          oemIdPreferencesDataSource.setIsGameFromGameshub(
              attributionFromGame.oemId == MiscProperties.GAME_FROM_GAMESHUB_OEMID)
          // Tries to send gamesHub's oemid, if available. Otherwise sends the oemid of the game.
          oemIdExtractorService.extractOemId(defaultGamesHubPackage).map { gamesHubOemId ->
            if (gamesHubOemId.isEmpty()) {
              attributionFromGame
            } else {
              AttributionEntity(gamesHubOemId.ifEmpty { null }, attributionFromGame.domain)
            }
          }
        }
        .flatMap { attribution ->
          // if game's package is in the cached-apks list, use the oemId from cache.
          getPackagesForClientSide()
              .onErrorReturn { listOf<String>() }
              .map { packagesForCaching ->
                if (packagesForCaching.any { it == packageName }) {
                  // if there is an oemId in the cache, use it instead of the one extracted from the
                  // game.
                  val oemIdFromCache = oemIdPreferencesDataSource.getOemIdForPackage(packageName)
                  if (oemIdFromCache.isBlank()) {
                    // save the oemId extracted from the game in the cache.
                    oemIdPreferencesDataSource.setOemIdForPackage(
                        packageName, attribution.oemId ?: "")
                    attribution
                  } else {
                    AttributionEntity(oemIdFromCache, attribution.domain)
                  }
                } else {
                  // don't use cache
                  attribution
                }
              }
        }
  }

  fun getPackagesForClientSide(): Single<List<String?>> {
    if (oemIdPreferencesDataSource.getPackageListClientSide().isEmpty() ||
        (System.currentTimeMillis() - oemIdPreferencesDataSource.getLastTimePackagesForCaching()) >
            MAX_AGE_CLIENT_SIDE_PACKAGE_LIST) {
      return partnerAttributionApi.fetchPackagesForCaching().map { packagesForCaching ->
        oemIdPreferencesDataSource.setPackageListClientSide(packagesForCaching)
        oemIdPreferencesDataSource.setLastTimePackagesForCaching(System.currentTimeMillis())
        packagesForCaching
      }
    } else {
      return Single.just(oemIdPreferencesDataSource.getPackageListClientSide())
    }
  }

  fun getOrSetOemIDFromGamesHub(): Single<String> {
    val ghOemIdIndicative = oemIdPreferencesDataSource.getGamesHubOemIdIndicative()
    return if (ghOemIdIndicative.isEmpty()) {
      if (installerService.isPackageInstalled(defaultGamesHubPackage)) {
        // Try to extract OemID if Games Hun installed
        getOemIdFromGamesHub().doOnSuccess {
          oemIdPreferencesDataSource.setGamesHubOemIdIndicative(it)
        }
      } else {
        oemIdPreferencesDataSource.setGamesHubOemIdIndicative(GH_NOT_INSTALLED)
        // If Games hub not installed return this text
        Single.just(GH_NOT_INSTALLED)
      }
    } else {
      Single.just(ghOemIdIndicative)
    }
  }

  private fun getOemIdFromGamesHub(): Single<String> {
    return oemIdExtractorService.extractOemId(defaultGamesHubPackage).map { gamesHubOemId ->
      gamesHubOemId.ifEmpty { GH_INSTALLED_WITHOUT_OEMID }
    }
  }

  fun isGameFromGamesHub(): Boolean {
    return oemIdPreferencesDataSource.getIsGameFromGameshub()
  }

  companion object {
    private const val DEFAULT_STORE_ADDRESS = "0xc41b4160b63d1f9488937f7b66640d2babdbf8ad"
    private const val DEFAULT_OEM_ADDRESS = "DEFAULT_OEM_ADDRESS"
    private const val MAX_AGE_CLIENT_SIDE_PACKAGE_LIST = 7 * 24 * 60 * 60 * 1000L // 1 week
    private const val GH_INSTALLED_WITHOUT_OEMID = "gh_installed_without_oemid"
    private const val GH_NOT_INSTALLED = "gh_not_installed"
  }
}
