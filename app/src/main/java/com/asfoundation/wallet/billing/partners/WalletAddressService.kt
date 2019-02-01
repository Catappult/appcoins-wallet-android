package com.asfoundation.wallet.billing.partners

import io.reactivex.Single

interface WalletAddressService {
  fun getWalletAddressForPackage(packageName: String): Single<String>
}
