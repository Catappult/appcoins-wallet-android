package com.asfoundation.wallet.backup

import android.os.Bundle
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asf.wallet.R
import com.asfoundation.wallet.backup.bottomSheet.BackupSaveOnDeviceBottomSheetFragment
import javax.inject.Inject

class BackupSaveOptionsNavigator @Inject constructor(private val navController: NavController) :
  Navigator {
  fun showWalletSuccessScreen() {
    navigate(
      navController,
      BackupSaveOptionsComposeFragmentDirections.actionBackupOptionsToSuccessScreen()
    )
  }

  fun showErrorScreen() {
    navigate(
      navController,
      BackupSaveOptionsComposeFragmentDirections.actionBackupOptionsToErrorScreen()
    )
  }

  fun showSaveOnDeviceFragment(walletAddress: String, password: String?) {
    val bundle = Bundle()
    bundle.putString(BackupSaveOnDeviceBottomSheetFragment.WALLET_ADDRESS_KEY, walletAddress)
    bundle.putString(BackupSaveOnDeviceBottomSheetFragment.PASSWORD_KEY, password)
    navController.navigate(R.id.action_backup_options_to_save_on_device, bundle)
  }
}
