package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Single

internal interface Billing {
  fun isSubsSupported(packageName: String): Single<BillingSupportType>
  fun isInAppSupported(packageName: String): Single<BillingSupportType>
  fun getProducts(skus: List<String>, type: String): Single<List<Product>>

  enum class BillingSupportType {
    SUPPORTED, MERCHANT_NOT_FOUND, UNKNOWN_ERROR, NO_INTERNET_CONNECTION, API_ERROR
  }
}