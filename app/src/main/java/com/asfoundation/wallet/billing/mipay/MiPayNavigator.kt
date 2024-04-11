package com.asfoundation.wallet.billing.mipay

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.appcoins.wallet.core.arch.data.Navigator
import com.asfoundation.wallet.ui.iab.WebViewActivity
import javax.inject.Inject

class MiPayNavigator @Inject constructor(
  private val fragment: Fragment,
) :
  Navigator {

  fun navigateToWebView(url: String, webViewLauncher: ActivityResultLauncher<Intent>) {
    webViewLauncher.launch(WebViewActivity.newIntent(fragment.requireActivity(), url))
  }
}