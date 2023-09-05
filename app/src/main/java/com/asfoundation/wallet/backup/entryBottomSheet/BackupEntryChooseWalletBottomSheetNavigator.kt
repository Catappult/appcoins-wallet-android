package com.asfoundation.wallet.backup.entryBottomSheet

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.feature.backup.ui.entry.BackupEntryFragment
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
    bundle.putString(BackupEntryFragment.WALLET_ADDRESS_KEY, walletAddress)
    bundle.putString(BackupEntryFragment.WALLET_NAME, walletName)
    mainNavController.navigate(R.id.action_back_to_entry, bundle)
  }
}
