package com.asfoundation.wallet.promotions.voucher

import android.content.Context
import android.content.Intent
import android.net.Uri

class EVoucherDetailsNavigator(private val context: Context) {
  fun navigateToNextScreen() {
    TODO("Not yet implemented")
  }

  fun navigateBack() {
    TODO("Not yet implemented")
  }

  fun navigateToStore(packageName: String) {
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=" + packageName)
        )
    )
  }

}
