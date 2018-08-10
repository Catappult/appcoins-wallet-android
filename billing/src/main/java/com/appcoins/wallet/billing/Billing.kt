package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Gateway
import com.appcoins.wallet.billing.repository.entity.Product
import com.appcoins.wallet.billing.repository.entity.Purchase
import io.reactivex.Scheduler
import io.reactivex.Single

interface Billing {

  fun isSubsSupported(): Single<BillingSupportType>

  fun isInAppSupported(): Single<BillingSupportType>

  fun getProducts(skus: List<String>, type: String): Single<List<Product>>

  fun getSkuTransactionStatus(sku: String, scheduler: Scheduler): Single<String>

  fun getSkuPurchase(sku: String, scheduler: Scheduler): Single<Purchase>

  fun getPurchases(type: BillingSupportedType, scheduler: Scheduler): Single<List<Purchase>>

  fun consumePurchases(purchaseToken: String, scheduler: Scheduler): Single<Boolean>

  fun getGateways(): Single<List<Gateway>>

  enum class BillingSupportType {
    SUPPORTED, MERCHANT_NOT_FOUND, UNKNOWN_ERROR, NO_INTERNET_CONNECTION, API_ERROR
  }
}