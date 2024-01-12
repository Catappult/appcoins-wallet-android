package com.asfoundation.wallet.billing.googlepay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.asfoundation.wallet.billing.googlepay.models.GooglePayResult
import com.asfoundation.wallet.billing.googlepay.repository.GooglePayWebRepository
import com.wallet.appcoins.core.legacy_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GooglePayReturnActivity(
  private val googlePayWebRepository: GooglePayWebRepository
) : BaseActivity() {
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
          Log.d(TAG, "cancel")
          googlePayWebRepository.saveChromeResult(GooglePayResult.CANCEL.key)
        }
        GooglePayResult.ERROR.key -> {
          Log.d(TAG, "error")
          googlePayWebRepository.saveChromeResult(GooglePayResult.ERROR.key)
        }
        else -> {
          Log.d(TAG, "else")
          googlePayWebRepository.saveChromeResult(GooglePayResult.ERROR.key)
        }
      }
    }
    finish()
  }

  companion object {
    val TAG = GooglePayReturnActivity::class.java.name
  }
}
