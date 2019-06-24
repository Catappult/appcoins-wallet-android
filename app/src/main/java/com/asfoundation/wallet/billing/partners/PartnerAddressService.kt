package com.asfoundation.wallet.billing.partners

import com.asfoundation.wallet.util.DeviceInfo
import io.reactivex.Single


class PartnerAddressService(private val installerService: InstallerService,
                            private val walletAddressService: WalletAddressService,
                            private val deviceInfo: DeviceInfo) :
    AddressService {

  override fun getStoreAddressForPackage(packageName: String): Single<String> {
    return installerService.getInstallerPackageName(packageName)
        .flatMap { installerPackageName ->
          walletAddressService.getStoreWalletForPackage(installerPackageName)
        }.onErrorResumeNext { walletAddressService.getStoreDefaultAddress() }
  }

  override fun getOemAddressForPackage(packageName: String): Single<String> {
    return installerService.getInstallerPackageName(packageName)
        .flatMap { installerPackageName ->
          walletAddressService.getOemWalletForPackage(installerPackageName,
              deviceInfo.manufacturer, deviceInfo.model)
        }.onErrorResumeNext { walletAddressService.getOemDefaultAddress() }
  }
}