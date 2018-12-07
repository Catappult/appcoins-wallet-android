package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethod
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Scheduler
import io.reactivex.Single

interface Billing {

  fun isSubsSupported(merchantName: String): Single<BillingSupportType>

  fun isInAppSupported(merchantName: String): Single<BillingSupportType>

  fun getProducts(merchantName: String, skus: List<String>): Single<List<Product>>

  fun getAppcoinsTransaction(uid: String, scheduler: Scheduler): Single<Transaction>

  fun getSkuPurchase(merchantName: String, sku: String, scheduler: Scheduler): Single<Purchase>

  fun getPurchases(merchantName: String, type: BillingSupportedType,
                   scheduler: Scheduler): Single<List<Purchase>>

  fun consumePurchases(merchantName: String, purchaseToken: String,
                       scheduler: Scheduler): Single<Boolean>

  fun getPaymentMethods(): Single<List<PaymentMethod>>

  enum class BillingSupportType {
    SUPPORTED, MERCHANT_NOT_FOUND, UNKNOWN_ERROR, NO_INTERNET_CONNECTION, API_ERROR
  }

  fun getSkuTransaction(merchantName: String, sku: String,
                        scheduler: Scheduler): Single<Transaction>

  fun getWallet(packageName: String): Single<String>
}