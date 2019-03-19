package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.WebViewActivity

class TopUpActivityPresenter(private val view: TopUpActivityView) {
  fun present() {
      view.showTopUpScreen()
  }

  fun processActivityResult(requestCode: Int, resultCode: Int) {
    if (requestCode == TopUpActivity.WEB_VIEW_REQUEST_CODE) {
      if (resultCode == WebViewActivity.FAIL) {
        view.close()
      }
    }
  }
}
