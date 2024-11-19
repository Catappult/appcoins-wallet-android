package com.asfoundation.wallet.iab.presentation.payment_methods.credit_card

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.ui.iab.WebViewActivity

class AdyenResultContract(private val paymentModel: PaymentModel) :
  ActivityResultContract<String, ActionResolution>() {

  override fun createIntent(context: Context, input: String): Intent =
    WebViewActivity.newIntent(context as ComponentActivity, input)

  override fun parseResult(resultCode: Int, intent: Intent?): ActionResolution =
    when (resultCode) {
      WebViewActivity.SUCCESS ->
        ActionResolution.Success(intent?.data!!, paymentModel)

      WebViewActivity.USER_CANCEL ->
        ActionResolution.Cancel

      else ->
        ActionResolution.Fail
    }
}

sealed class ActionResolution {
  data object Cancel : ActionResolution()

  data object Fail : ActionResolution()

  data class Success(val data: Uri, val paymentModel: PaymentModel) : ActionResolution()
}
