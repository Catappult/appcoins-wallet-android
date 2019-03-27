package com.asfoundation.wallet.topup

import android.content.Intent
import com.asfoundation.wallet.ui.iab.WebViewActivity

class TopUpActivityPresenter(private val view: TopUpActivityView) {
  fun present(isCreating: Boolean) {
    if (isCreating) {
      view.showTopUpScreen()
    }
  }

  fun processActivityResult(requestCode: Int, resultCode: Int,
                            data: Intent?) {
    if (requestCode == TopUpActivity.WEB_VIEW_REQUEST_CODE) {
      if (resultCode == WebViewActivity.FAIL) {
        view.close()
      } else if (resultCode == WebViewActivity.SUCCESS && data!= null) {
        view.acceptResult(data.data)
      }
    }
  }
}
