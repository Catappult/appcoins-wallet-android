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

  override fun getStoreWalletForPackage(packageName: String?, manufacturer: String?, model: String?,
                                        storeId: String?): Single<String> {
    return bdsApi.getStoreWallet(packageName, manufacturer, model, storeId)
        .map { it.items[0].user.wallet_address }
        .onErrorReturn { defaultStoreAddress }
  }

  override fun getOemWalletForPackage(packageName: String?, manufacturer: String?,
                                      model: String?,
                                      storeId: String?): Single<String> {
    return bdsApi.getOemWallet(packageName, manufacturer, model, storeId)
        .map { it.items[0].user.wallet_address }
        .onErrorReturn { defaultOemAddress }
  }
}