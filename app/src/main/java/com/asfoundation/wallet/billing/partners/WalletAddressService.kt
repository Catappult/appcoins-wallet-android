package com.asfoundation.wallet.billing.partners

import io.reactivex.Single

interface WalletAddressService {
  fun getStoreDefaultAddress(): Single<String>

  fun getOemDefaultAddress(): Single<String>

  fun getStoreWalletForPackage(packageName: String): Single<String>

  fun getOemWalletForPackage(packageName: String, manufacturer: String?,
                             model: String?): Single<String>
}
