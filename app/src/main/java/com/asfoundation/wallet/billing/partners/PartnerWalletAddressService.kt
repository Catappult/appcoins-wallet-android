package com.asfoundation.wallet.billing.partners

import io.reactivex.Single

class PartnerWalletAddressService(private val bdsApi: BdsPartnersApi,
                                  private val defaultStoreAddress: String,
                                  private val defaultOemAddress: String) : WalletAddressService {

  override fun getStoreDefaultAddress(): Single<String> {
    return Single.just(defaultStoreAddress)
  }

  override fun getOemDefaultAddress(): Single<String> {
    return Single.just(defaultOemAddress)
  }

  override fun getStoreWalletForPackage(packageName: String): Single<String> {
    return if (packageName.isEmpty()) Single.just(defaultStoreAddress)
    else bdsApi.getStoreWallet(packageName).map { it.items[0].user.wallet_address }
        .onErrorReturn { defaultStoreAddress }
  }

  override fun getOemWalletForPackage(packageName: String, manufacturer: String?,
                                      model: String?): Single<String> {
    return if (packageName.isEmpty()) Single.just(defaultOemAddress)
    else bdsApi.getOemWallet(packageName, manufacturer, model).map { it.items[0].user.wallet_address }
        .onErrorReturn { defaultOemAddress }
  }
}