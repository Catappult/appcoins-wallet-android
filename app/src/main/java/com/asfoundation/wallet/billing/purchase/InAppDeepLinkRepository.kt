package com.asfoundation.wallet.billing.purchase

import io.reactivex.Single

interface InAppDeepLinkRepository {

  /**
   * All optional fields should be passed despite possible being null as these are
   * required by some applications to complete the purchase flow
   * @param domain package name of the application
   * @param skuId name of the product that is being bought
   * @param userWalletAddress address of the user wallet
   * @param signature signature obtained after signing the wallet
   * @param originalAmount amount of the transaction. Only needed in one step payments
   * @param originalCurrency currency of the transaction. Only needed in one step payments
   * @param paymentMethod Name of the payment method being used
   * @param developerWalletAddress Wallet address of the apps developer. Null on topup
   * @param storeWalletAddress Wallet address of the store from which the app was downloaded. Null on topup
   * @param oemWalletAddress Wallet address of the original equipment manufacturer. Null on topup
   * @param callbackUrl url used in some purchases by the application to complete the purchase
   * @param orderReference reference used in some purchases by the application to
   * complete the purchase
   * @param payload Group of details used in some purchases by the application to
   * complete the purchase
   */
  fun getDeepLink(domain: String, skuId: String?, userWalletAddress: String,
                  signature: String, originalAmount: String?, originalCurrency: String?,
                  paymentMethod: String, developerWalletAddress: String?,
                  storeWalletAddress: String?, oemWalletAddress: String?,
                  callbackUrl: String?, orderReference: String?,
                  payload: String?): Single<String>

}
