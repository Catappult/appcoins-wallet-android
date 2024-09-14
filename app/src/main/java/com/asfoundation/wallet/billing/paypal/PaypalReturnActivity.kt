package com.asfoundation.wallet.billing.paypal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.asfoundation.wallet.billing.googlepay.models.CustomTabsPayResult
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaypalReturnActivity : BaseActivity() {
  @Inject
  lateinit var payPalV2Repository: PayPalV2Repository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val data = intent.data
    if (Intent.ACTION_VIEW == intent.action && data != null) {
      val redirectResult = data.getQueryParameter("redirectResult")
      when (redirectResult) {
        CustomTabsPayResult.SUCCESS.key -> {
          Log.d(TAG, "success")
          payPalV2Repository.saveChromeResult(CustomTabsPayResult.SUCCESS.key)
        }

        CustomTabsPayResult.CANCEL.key -> {
          payPalV2Repository.saveChromeResult(CustomTabsPayResult.CANCEL.key)
        }

        CustomTabsPayResult.ERROR.key -> {
          payPalV2Repository.saveChromeResult(CustomTabsPayResult.ERROR.key)
        }

        else -> {
          payPalV2Repository.saveChromeResult(CustomTabsPayResult.ERROR.key)
        }
      }
    }
    finish()
  }

  companion object {
    val TAG = PaypalReturnActivity::class.java.name
    const val PAYPAL_TIMEOUT = 16000L
  }
}
