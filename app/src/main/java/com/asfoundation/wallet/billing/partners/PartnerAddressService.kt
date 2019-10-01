package com.asfoundation.wallet.billing.partners

import com.asfoundation.wallet.util.DeviceInfo
import io.reactivex.Single
import io.reactivex.functions.BiFunction


class PartnerAddressService(private val installerService: InstallerService,
                            private val walletAddressService: WalletAddressService,
                            private val deviceInfo: DeviceInfo,
                            private val apkFyService: ApkFyService) : AddressService {

  override fun getStoreAddressForPackage(packageName: String): Single<String> {
    return Single.zip(installerService.getInstallerPackageName(packageName),
        apkFyService.extractOemId(packageName),
        BiFunction { t1: String, t2: String -> Pair(t1, t2) }
    )
        .flatMap { pair ->
          walletAddressService.getStoreWalletForPackage(pair.first.ifEmpty { null },
              deviceInfo.manufacturer, deviceInfo.model, pair.second.ifEmpty { null })
        }
        .onErrorResumeNext { walletAddressService.getStoreDefaultAddress() }
  }

  override fun getOemAddressForPackage(packageName: String): Single<String> {
    return Single.zip(installerService.getInstallerPackageName(packageName),
        apkFyService.extractOemId(packageName),
        BiFunction { t1: String, t2: String -> Pair(t1, t2) }
    )
        .flatMap { pair ->
          walletAddressService.getOemWalletForPackage(pair.first.ifEmpty { null },
              deviceInfo.manufacturer, deviceInfo.model, pair.second.ifEmpty { null })
        }
        .onErrorResumeNext { walletAddressService.getOemDefaultAddress() }
  }

}