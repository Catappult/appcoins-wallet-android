package com.asfoundation.wallet.backup.BottomSheet
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class BackupSaveOnDeviceBottomSheetNavigator @Inject constructor(private val fragment: Fragment,
) : Navigator {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }
  fun navigateToSuccessScreen(navController: NavController) {
    navController.navigate(R.id.backup_wallet_success_screen)
  }

}