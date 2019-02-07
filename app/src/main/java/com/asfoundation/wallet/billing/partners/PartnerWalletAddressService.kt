package com.asfoundation.wallet.billing.partners

import io.reactivex.Single

class PartnerWalletAddressService(private val bdsApi: BdsPartnersApi,
                                  private val defaultAddress: String) : WalletAddressService {
  override fun getDefaultAddress(): Single<String> {
    return Single.just(defaultAddress)
  }

  override fun getWalletAddressForPackage(packageName: String): Single<String> {
    return if (packageName.isEmpty()) Single.just(defaultAddress)
    else bdsApi.getWallet(packageName).map { it.items[0].user.wallet_address }
        .onErrorReturn { defaultAddress }
  }
}