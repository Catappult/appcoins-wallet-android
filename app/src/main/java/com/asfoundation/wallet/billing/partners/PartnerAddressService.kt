package com.asfoundation.wallet.billing.partners

import com.asfoundation.wallet.util.DeviceUtils
import io.reactivex.Single


class PartnerAddressService(private val installerService: InstallerService,
                            private val walletAddressService: WalletAddressService,
                            private val utils: DeviceUtils) :
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
              utils.deviceManufacturer, utils.deviceModel)
        }.onErrorResumeNext { walletAddressService.getOemDefaultAddress() }
  }
}