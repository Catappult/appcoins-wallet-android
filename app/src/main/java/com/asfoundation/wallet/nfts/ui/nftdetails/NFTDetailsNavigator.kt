package com.asfoundation.wallet.nfts.ui.nftdetails

import androidx.navigation.NavController
import com.appcoins.wallet.ui.arch.Navigator
import com.appcoins.wallet.ui.arch.navigate
import com.asfoundation.wallet.nfts.domain.NFTItem
import javax.inject.Inject

class NFTDetailsNavigator @Inject constructor(private val navController: NavController) :
  Navigator {

  fun navigateBack() {
    navController.popBackStack()
  }

  fun navigateToTransact(data: NFTItem) {
    navigate(navController, NFTDetailsFragmentDirections.actionNavigateToTransact(data))
  }
}