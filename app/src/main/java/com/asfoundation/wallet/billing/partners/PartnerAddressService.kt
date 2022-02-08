package com.asfoundation.wallet.billing.partners

import com.asfoundation.wallet.util.DeviceInfo
import io.reactivex.Single


class PartnerAddressService(private val installerService: InstallerService,
                            private val deviceInfo: DeviceInfo,
                            private val oemIdExtractorService: OemIdExtractorService,
                            private val defaultStoreAddress: String,
                            private val defaultOemAddress: String) :
    AddressService {

  override fun getStoreAddress(suggestedStoreAddress: String?): String {
    return suggestedStoreAddress?.let { suggestedStoreAddress } ?: defaultStoreAddress
  }

  override fun getOemAddress(suggestedOemAddress: String?): String {
    return suggestedOemAddress?.let { suggestedOemAddress } ?: defaultOemAddress
  }

  override fun getAttributionEntity(packageName: String): Single<AttributionEntity> {
    return Single.zip(installerService.getInstallerPackageName(packageName),
        oemIdExtractorService.extractOemId(packageName),
        { installerPackage, oemId ->
          AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
        })
  }
}