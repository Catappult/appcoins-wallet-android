package com.appcoins.wallet.core.analytics.analytics.partners

import io.reactivex.Single

interface AddressService {
  fun getStoreAddress(suggestedStoreAddress: String?): String

  fun getOemAddress(suggestedOemAddress: String?): String

  fun getAttribution(packageName: String): Single<AttributionEntity>

  fun getAttributionClientCache(packageName: String): Single<AttributionEntity>
}
