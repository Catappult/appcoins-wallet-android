package com.asfoundation.wallet.promotions.voucher

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentManager

class EVoucherDetailsNavigator(private val fragmentManager: FragmentManager,
                               private val context: Context) {
  fun navigateToNextScreen() {
    TODO("Not yet implemented")
  }

  fun navigateBack() {
    fragmentManager.popBackStack()
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
