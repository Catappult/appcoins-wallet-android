package com.asfoundation.wallet.my_wallets.verify_picker

import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
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