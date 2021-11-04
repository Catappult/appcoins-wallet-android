package com.asfoundation.wallet.nfts.ui.nftdetails

import androidx.navigation.NavController
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.base.navigate
import com.asfoundation.wallet.my_wallets.main.MyWalletsFragmentDirections

class NFTDetailsNavigator(private val navController: NavController) : Navigator {

    fun navigateBack(){
        navController.popBackStack()
    }
}