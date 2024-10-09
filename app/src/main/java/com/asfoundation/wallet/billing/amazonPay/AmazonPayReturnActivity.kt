package com.asfoundation.wallet.billing.amazonPay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.asfoundation.wallet.billing.amazonPay.models.AmazonPayResult
import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
import com.asfoundation.wallet.billing.googlepay.models.GooglePayResult
import com.asfoundation.wallet.billing.googlepay.repository.GooglePayWebRepository
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AmazonPayReturnActivity : BaseActivity() {
  @Inject
  lateinit var amazonPayRepository: AmazonPayRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val data = intent.data
    Log.d("amazonpaytransaction", "AmazonPayReturnActivity oncreate: " + data)
    if (Intent.ACTION_VIEW == intent.action && data != null) {
      val redirectResult = data.getQueryParameter("amazonCheckoutSessionId")
      when (redirectResult) {
        AmazonPayResult.SUCCESS.key -> {
          Log.d("amazonpaytransaction", "success")
          amazonPayRepository.saveChromeResult(AmazonPayResult.SUCCESS.key, "")
        }

        AmazonPayResult.CANCEL.key -> {
          amazonPayRepository.saveChromeResult(AmazonPayResult.CANCEL.key, "")
        }

        AmazonPayResult.ERROR.key -> {
          amazonPayRepository.saveChromeResult(AmazonPayResult.ERROR.key, "")
        }

        else -> {
          amazonPayRepository.saveChromeResult(AmazonPayResult.ERROR.key, "")
        }
      }
    }
    finish()
  }

  companion object {
    val TAG = AmazonPayReturnActivity::class.java.name
  }
}
