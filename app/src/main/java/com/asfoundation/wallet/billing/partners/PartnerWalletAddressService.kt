package com.asfoundation.wallet.billing.partners

import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import io.reactivex.Single

class PartnerWalletAddressService(private val bdsApi: BdsApiSecondary,
                                  private val defaultAddress: String) :
    WalletAddressService {
  override fun getWalletAddressForPackage(packageName: String): Single<String> {
    return if (packageName.isEmpty()) Single.just(defaultAddress)
    else bdsApi.getWallet(packageName).map { it.data.address }
        .onErrorReturn { defaultAddress }
  }
}