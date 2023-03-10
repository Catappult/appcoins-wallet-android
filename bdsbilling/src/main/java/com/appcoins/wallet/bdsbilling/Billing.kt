package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.appcoins.wallet.bdsbilling.repository.entity.Product
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Scheduler
import io.reactivex.Single

interface Billing {

  fun isSubsSupported(merchantName: String): Single<BillingSupportType>

  fun isInAppSupported(merchantName: String): Single<BillingSupportType>

  fun getProducts(
    merchantName: String, skus: List<String>,
    type: BillingSupportedType
  ): Single<List<Product>>

  fun getAppcoinsTransaction(uid: String, scheduler: Scheduler): Single<Transaction>

  fun getSkuPurchase(
    merchantName: String, sku: String?, purchaseUid: String?, scheduler: Scheduler,
    type: BillingSupportedType
  ): Single<Purchase>

  fun getPurchases(
    packageName: String, type: BillingSupportedType,
    scheduler: Scheduler
  ): Single<List<Purchase>>

  fun consumePurchases(
    merchantName: String, purchaseToken: String,
    scheduler: Scheduler, type: BillingSupportedType?
  ): Single<Boolean>

  fun getPaymentMethods(
    value: String,
    currency: String,
    transactionType: String,
    packageName: String
  ): Single<List<PaymentMethodEntity>>

  enum class BillingSupportType {
    SUPPORTED, MERCHANT_NOT_FOUND, UNKNOWN_ERROR, NO_INTERNET_CONNECTION, API_ERROR
  }

  fun getSkuTransaction(
    merchantName: String, sku: String?,
    scheduler: Scheduler, type: BillingSupportedType
  ): Single<Transaction>

  fun getWallet(packageName: String): Single<String>

  fun getSubscriptionToken(
    packageName: String, skuId: String,
    networkThread: Scheduler
  ): Single<String>
}