package com.asfoundation.wallet.backup

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.core.arch.data.Navigator
import com.appcoins.wallet.core.arch.data.navigate
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BottomSheet.BackupSaveOnDeviceBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class BackupSaveOptionsNavigator @Inject constructor(
  private val fragment: Fragment,
  private val navController: NavController
) : Navigator {


  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun handleBackPress() {
    navController.popBackStack()
  }

  fun showWalletSuccessScreen() {
    navigate(
      navController,
      BackupSaveOptionsComposeFragmentDirections.actionBackupOptionsToSuccessScreen()
    )

  }
  fun showErrorScreen(){
    navigate(
      navController,
      BackupSaveOptionsComposeFragmentDirections.actionBackupOptionsToErrorScreen()
    )
  }

  fun showSaveOnDeviceFragment(
    walletAddress : String,
    password : String?,
    mainNavController : NavController
  ) {
    val bundle = Bundle()
    bundle.putString(BackupSaveOnDeviceBottomSheetFragment.WALLET_ADDRESS_KEY, walletAddress)
    bundle.putString(BackupSaveOnDeviceBottomSheetFragment.PASSWORD_KEY, password)
    mainNavController.navigate(R.id.action_backup_options_to_save_on_device, bundle)
        }
}