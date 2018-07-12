package com.appcoins.wallet.billing

import io.reactivex.Single

internal interface Billing {
  fun isSubsSupported(packageName: String): Single<BillingSupportType>
  fun isInAppSupported(packageName: String): Single<BillingSupportType>

  enum class BillingSupportType {
    SUPPORTED, MERCHANT_NOT_FOUND, UNKNOWN_ERROR
  }
}