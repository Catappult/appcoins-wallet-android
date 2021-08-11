package com.asfoundation.wallet.billing.partners

import io.reactivex.Single

interface WalletAddressService {
  fun getStoreDefaultAddress(): Single<String>

  fun getOemDefaultAddress(): Single<String>

  fun getStoreWalletForPackage(packageName: String?, manufacturer: String?, model: String?,
                               storeId: String?): Single<String>

  fun getOemWalletForPackage(packageName: String?, manufacturer: String?,
                             model: String?,
                             storeId: String?): Single<String>
}
