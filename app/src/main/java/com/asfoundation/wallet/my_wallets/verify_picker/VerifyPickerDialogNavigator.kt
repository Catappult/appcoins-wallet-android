package com.asfoundation.wallet.my_wallets.verify_picker

import androidx.navigation.NavController
import com.appcoins.wallet.ui.arch.Navigator
import com.appcoins.wallet.ui.arch.navigate
import javax.inject.Inject

class VerifyPickerDialogNavigator @Inject constructor(private val navController: NavController) :
  Navigator {

  fun navigateToCreditCardVerify() {
    navigate(navController, VerifyPickerDialogFragmentDirections.actionNavigateToVerifyCard(false))
  }

  fun navigateToPaypalVerify() {
    navigate(navController, VerifyPickerDialogFragmentDirections.actionNavigateToVerifyPaypal())
  }
}