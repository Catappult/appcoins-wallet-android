package com.asfoundation.wallet.billing.googlepay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.asfoundation.wallet.billing.googlepay.models.GooglePayResult
import com.asfoundation.wallet.billing.googlepay.repository.GooglePayWebRepository
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GooglePayReturnActivity : BaseActivity() {
  @Inject
  lateinit var googlePayWebRepository: GooglePayWebRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val data = intent.data
    if (Intent.ACTION_VIEW == intent.action && data != null) {
      val redirectResult = data.getQueryParameter("redirectResult")
      when (redirectResult) {
        GooglePayResult.SUCCESS.key -> {
          Log.d(TAG, "success")
          googlePayWebRepository.saveChromeResult(GooglePayResult.SUCCESS.key)
        }

        GooglePayResult.CANCEL.key -> {
          googlePayWebRepository.saveChromeResult(GooglePayResult.CANCEL.key)
        }

        GooglePayResult.ERROR.key -> {
          googlePayWebRepository.saveChromeResult(GooglePayResult.ERROR.key)
        }

        else -> {
          googlePayWebRepository.saveChromeResult(GooglePayResult.ERROR.key)
        }
      }
    }
    finish()
  }

  companion object {
    val TAG: String? = GooglePayReturnActivity::class.java.name
  }
}
