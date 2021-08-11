package com.asfoundation.wallet.billing.partners

import io.reactivex.Single

interface AddressService {
  fun getStoreAddressForPackage(packageName: String): Single<String>

  fun getOemAddressForPackage(packageName: String): Single<String>
}