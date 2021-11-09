package com.asfoundation.wallet.nfts.ui.nftdetails

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator

class NFTDetailsNavigator(private val navController: NavController) : Navigator {

  fun navigateBack() {
    navController.popBackStack()
  }
}