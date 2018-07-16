package com.appcoins.wallet.billing

import android.os.Bundle
import com.appcoins.wallet.billing.repository.entity.ProductsDetail

internal class BillingMessagesMapper {
  fun mapSkuDetails(skuDetails: ProductsDetail): Bundle {
    val bundle = Bundle()
    return bundle
  }

  internal fun mapSupported(supportType: Billing.BillingSupportType): Int =
      when (supportType) {
        Billing.BillingSupportType.SUPPORTED -> AppcoinsBillingBinder.RESULT_OK
        Billing.BillingSupportType.MERCHANT_NOT_FOUND -> AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE
        Billing.BillingSupportType.UNKNOWN_ERROR -> AppcoinsBillingBinder.RESULT_BILLING_UNAVAILABLE
        Billing.BillingSupportType.NO_INTERNET_CONNECTION -> AppcoinsBillingBinder.RESULT_SERVICE_UNAVAILABLE
        Billing.BillingSupportType.API_ERROR -> AppcoinsBillingBinder.RESULT_ERROR
      }

}