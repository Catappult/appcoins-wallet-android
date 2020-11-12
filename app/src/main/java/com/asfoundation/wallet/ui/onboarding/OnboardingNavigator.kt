package com.asfoundation.wallet.ui.onboarding

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.asf.wallet.R
import com.asfoundation.wallet.router.TransactionsRouter

class OnboardingNavigator(private val context: Context,
                          private val transactionsRouter: TransactionsRouter) {

  fun launchBrowser(uri: String) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT)
          .show()
    }
  }

  fun navigateToTransactions() = transactionsRouter.open(context, true)
}
