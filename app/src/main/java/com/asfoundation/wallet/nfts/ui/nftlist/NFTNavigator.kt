package com.asfoundation.wallet.nfts.ui.nftlist

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.nfts.domain.NFTItem

class NFTNavigator(private val navController: NavController) : Navigator {

  fun navigateToInfo(data: NFTItem) {
    navigate(navController, NFTFragmentDirections.actionNavigateToNft(data))
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}