package com.asfoundation.wallet.billing.partners

import io.reactivex.Single


class PartnerAddressService(private val installerService: InstallerService,
                            private val walletAddressService: WalletAddressService) :
    AddressService {

  override fun getStoreAddressForPackage(packageName: String): Single<String> {
    return installerService.getInstallerPackageName(packageName)
        .flatMap { installerPackageName ->
          walletAddressService.getWalletAddressForPackage(installerPackageName)
        }.onErrorResumeNext { walletAddressService.getDefaultAddress() }
  }
}