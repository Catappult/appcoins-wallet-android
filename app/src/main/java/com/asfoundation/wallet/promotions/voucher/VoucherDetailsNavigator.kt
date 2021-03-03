package com.asfoundation.wallet.promotions.voucher

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentManager

class VoucherDetailsNavigator(private val fragmentManager: FragmentManager,
                              private val context: Context) {
  fun navigateToPurchaseFlow() {
    //TODO
  }

  fun navigateBack() = fragmentManager.popBackStack()

  fun navigateToStore(packageName: String) {
    context.startActivity(
        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
    )
  }

}
