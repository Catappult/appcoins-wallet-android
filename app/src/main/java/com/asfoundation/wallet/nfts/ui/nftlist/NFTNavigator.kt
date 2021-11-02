package com.asfoundation.wallet.nfts.ui.nftlist

import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigator
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.nfts.domain.NFTItem

class NFTNavigator(private val navController: NavController) : Navigator {

  fun navigateToInfo(data : NFTItem , extras : FragmentNavigator.Extras) {
    navigate(navController, NFTFragmentDirections.actionNavigateToNft(data) , extras)
  }

  fun navigateBack(){
    navController.popBackStack()
  }
}