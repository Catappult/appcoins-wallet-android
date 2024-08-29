package com.asfoundation.wallet.iab

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import com.asf.wallet.R
import com.asfoundation.wallet.iab.domain.model.PurchaseData
import com.asfoundation.wallet.iab.error.IABError
import com.asfoundation.wallet.iab.presentation.main.MainFragmentArgs
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class PaymentActivityNavigator @AssistedInject constructor(
  @Assisted private val navController: NavController
) {

  fun navigate(navDirections: NavDirections) {
    navController.navigate(navDirections)
  }

  fun navigateToInitialScreen(purchaseData: PurchaseData?, error: IABError?) {
    navController.setGraph(
      graphResId = R.navigation.iab_graph,
      startDestinationArgs = MainFragmentArgs(purchaseData, error).toBundle(),
    )
  }
}
