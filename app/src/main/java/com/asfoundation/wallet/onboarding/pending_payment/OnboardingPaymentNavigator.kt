package com.asfoundation.wallet.onboarding.pending_payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
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
    currency: String
  ) {
    val bundle = Bundle().apply {
      putParcelable("transaction_builder", transactionBuilder)
      putString("package_name", packageName)
      putString("sku", sku)
      putString("amount", value.toString())
      putString("currency", currency)
    }
    navController.setGraph(R.navigation.inner_payment_graph, bundle)
  }
}