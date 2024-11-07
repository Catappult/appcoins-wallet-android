package com.asfoundation.wallet.billing.amazonPay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.amazonPay.repository.AmazonPayRepository
import com.asfoundation.wallet.topup.TopUpActivity
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
    launchApp(this, amazonPayRepository.getAmazonPayPaymentType())

  }

  fun launchApp(context: Context, packageName: String) {
    if(packageName == BuildConfig.APPLICATION_ID) {
      val intent = Intent(this, TopUpActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(intent)
      return
    }
      val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
      if (launchIntent != null) {
        context.startActivity(launchIntent)
      }
  }
}
