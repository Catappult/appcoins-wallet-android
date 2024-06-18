package com.asfoundation.wallet.onboarding.pending_payment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asf.wallet.R
import com.asfoundation.wallet.entity.TransactionBuilder
import javax.inject.Inject

class OnboardingPaymentNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {
  fun showPaymentMethods(
    navController: NavController,
    transactionBuilder: TransactionBuilder,
    packageName: String,
    sku: String,
    value: Double,
    currency: String,
    forecastBonus: ForecastBonusAndLevel
  ) {
    val bundle = Bundle().apply {
      putParcelable("transaction_builder", transactionBuilder)
      putString("package_name", packageName)
      putString("sku", sku)
      putString("amount", value.toString())
      putString("currency", currency)
      putSerializable("forecast_bonus", forecastBonus)
    }
    navController.setGraph(R.navigation.inner_payment_graph, bundle)
  }

  fun navigateToBrowser(uri: Uri) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      fragment.requireContext()
        .startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(fragment.requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
        .show()
    }
  }
}