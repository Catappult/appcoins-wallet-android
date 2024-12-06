package com.asfoundation.wallet.billing.amazonPay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
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
    if (Intent.ACTION_VIEW == intent.action && data != null) {
      val amazonCheckoutSessionId = data.getQueryParameter("amazonCheckoutSessionId")
      amazonPayRepository.saveResult(amazonCheckoutSessionId ?: "")
    }
    finish()
  }
}
