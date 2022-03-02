package com.asfoundation.wallet.my_wallets.verify_picker

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import javax.inject.Inject

class VerifyPickerDialogNavigator @Inject constructor(private val navController: NavController) : Navigator {

  fun navigateToCreditCardVerify() {
    navigate(navController, VerifyPickerDialogFragmentDirections.actionNavigateToVerifyCard(false))
  }

  fun navigateToPaypalVerify() {
    navigate(navController, VerifyPickerDialogFragmentDirections.actionNavigateToVerifyPaypal())
  }
}