package com.appcoins.wallet.billing

import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class AndroidBilling(private val billing: Billing) {
  fun isInAppSupported(merchantName: String): Single<Billing.BillingSupportType> {
    return billing.isInAppSupported(merchantName)
  }

  fun isSubsSupported(merchantName: String): Single<Billing.BillingSupportType> {
    return billing.isSubsSupported(merchantName)
  }

  fun getProducts(merchantName: String, skus: List<String>): Single<List<Product>> {
    return billing.getProducts(merchantName, skus)
  }

  fun getPurchases(merchantName: String,
                   type: BillingSupportedType): Single<List<Purchase>> {
    return billing.getPurchases(merchantName, type, Schedulers.io())
  }

  fun consumePurchases(purchaseToken: String, merchantName: String): Single<Boolean> {
    return billing.consumePurchases(merchantName, purchaseToken, Schedulers.io())
  }

  fun getDeveloperAddress(packageName: String): Single<String> {
    return billing.getWallet(packageName)
  }

}
