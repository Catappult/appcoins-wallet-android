package com.asfoundation.wallet.billing.partners

import com.asfoundation.wallet.util.DeviceInfo
import io.reactivex.Single


class PartnerAddressService(private val installerService: InstallerService,
                            private val walletAddressService: WalletAddressService,
                            private val deviceInfo: DeviceInfo,
                            private val oemIdExtractorService: OemIdExtractorService) :
    AddressService {

  override fun getStoreAddressForPackage(packageName: String): Single<String> {
    return getAttributionEntity(packageName)
        .flatMap { entity ->
          walletAddressService.getStoreWalletForPackage(entity.domain.ifEmpty { null },
              deviceInfo.manufacturer, deviceInfo.model, entity.oemId.ifEmpty { null })
        }
        .onErrorResumeNext { walletAddressService.getStoreDefaultAddress() }
  }

  override fun getOemAddressForPackage(packageName: String): Single<String> {
    return getAttributionEntity(packageName)
        .flatMap { entity ->
          walletAddressService.getOemWalletForPackage(entity.domain.ifEmpty { null },
              deviceInfo.manufacturer, deviceInfo.model, entity.oemId.ifEmpty { null })
        }
        .onErrorResumeNext { walletAddressService.getOemDefaultAddress() }
  }

  override fun getAttributionEntity(packageName: String): Single<AttributionEntity> {
    return Single.zip(installerService.getInstallerPackageName(packageName),
        oemIdExtractorService.extractOemId(packageName),
        { installerPackage, oemId -> AttributionEntity(oemId, installerPackage) })
  }

}