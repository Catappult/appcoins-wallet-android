package com.asfoundation.wallet.billing.partners

import com.asfoundation.wallet.util.DeviceInfo
import io.reactivex.Single
import io.reactivex.functions.BiFunction


class PartnerAddressService(private val installerService: InstallerService,
                            private val walletAddressService: WalletAddressService,
                            private val deviceInfo: DeviceInfo,
                            private val oemIdExtractorService: OemIdExtractorService) :
    AddressService {

  override fun getStoreAddressForPackage(packageName: String): Single<String> {
    return Single.zip(installerService.getInstallerPackageName(packageName),
        oemIdExtractorService.extractOemId(packageName),
        BiFunction { installerPackage: String, oemId: String -> Pair(installerPackage, oemId) })
        .flatMap { pair ->
          walletAddressService.getStoreWalletForPackage(pair.first.ifEmpty { null },
              deviceInfo.manufacturer, deviceInfo.model, pair.second.ifEmpty { null })
        }
        .onErrorResumeNext { walletAddressService.getStoreDefaultAddress() }
  }

  override fun getOemAddressForPackage(packageName: String): Single<String> {
    return Single.zip(installerService.getInstallerPackageName(packageName),
        oemIdExtractorService.extractOemId(packageName),
        BiFunction { installerPackage: String, oemId: String -> Pair(installerPackage, oemId) })
        .flatMap { pair ->
          walletAddressService.getOemWalletForPackage(pair.first.ifEmpty { null },
              deviceInfo.manufacturer, deviceInfo.model, pair.second.ifEmpty { null })
        }
        .onErrorResumeNext { walletAddressService.getOemDefaultAddress() }
  }

}