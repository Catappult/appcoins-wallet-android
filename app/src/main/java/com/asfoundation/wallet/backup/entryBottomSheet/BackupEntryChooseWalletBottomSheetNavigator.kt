package com.asfoundation.wallet.backup.entryBottomSheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import javax.inject.Inject

class BackupEntryChooseWalletBottomSheetNavigator
@Inject
constructor(val fragmentManager: FragmentManager, val fragment: Fragment) : Navigator {

  fun navigateToBackup(
    walletAddress: String,
    walletName: String,
    mainNavController: NavController
  ) {
    val bundle = Bundle()
    bundle.putString(WALLET_ADDRESS_KEY, walletAddress)
    bundle.putString(WALLET_NAME, walletName)
    mainNavController.navigate(R.id.action_back_to_entry, bundle)
  }

  companion object {
    const val WALLET_ADDRESS_KEY = "wallet_address"
    const val WALLET_NAME = "wallet_name"
  }
}


