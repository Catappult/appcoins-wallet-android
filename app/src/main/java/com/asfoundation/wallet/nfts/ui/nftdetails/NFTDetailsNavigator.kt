package com.asfoundation.wallet.nfts.ui.nftdetails

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import javax.inject.Inject

class NFTDetailsNavigator @Inject constructor(private val navController: NavController) : Navigator {

  fun navigateBack() {
    navController.popBackStack()
  }
}