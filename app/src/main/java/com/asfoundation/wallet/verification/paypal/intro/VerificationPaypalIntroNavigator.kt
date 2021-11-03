package com.asfoundation.wallet.verification.paypal.intro

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.ui.iab.WebViewActivity

class VerificationPaypalIntroNavigator(private val fragment: VerificationPaypalIntroFragment) :
    Navigator, DefaultLifecycleObserver {

  private lateinit var paymentResultLauncher: ActivityResultLauncher<Intent>
  private var paymentResultCallback: ActivityResultCallback<ActivityResult>? = null

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    paymentResultLauncher =
        fragment.requireActivity().activityResultRegistry.register("WEBVIEW_PAYMENT", owner,
            ActivityResultContracts.StartActivityForResult()) { activityResult ->
          paymentResultCallback?.onActivityResult(activityResult)
        }
  }

  fun navigateToPayment(url: String, callback: ActivityResultCallback<ActivityResult>) {
    paymentResultCallback = callback
    paymentResultLauncher.launch(WebViewActivity.newIntent(fragment.activity, url))
  }

  fun navigateBack() {
    fragment.activity?.finish()
  }

}