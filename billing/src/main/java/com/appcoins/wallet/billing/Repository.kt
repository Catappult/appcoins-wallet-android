package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.BillingSupportedType
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Single

internal interface Repository {
  fun isSupported(packageName: String, type: BillingSupportedType): Single<Boolean>
  fun getSkuDetails(packageName: String, skuIds: List<String>,
                    type: BillingType): Single<List<Product>>

  enum class BillingType {
    inapp, subs
  }
}
