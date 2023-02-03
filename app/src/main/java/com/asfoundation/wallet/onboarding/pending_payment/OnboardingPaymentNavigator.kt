package com.asfoundation.wallet.onboarding.pending_payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.entity.TransactionBuilder
import javax.inject.Inject

class OnboardingPaymentNavigator @Inject constructor(private val fragment: Fragment) : Navigator {
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
}