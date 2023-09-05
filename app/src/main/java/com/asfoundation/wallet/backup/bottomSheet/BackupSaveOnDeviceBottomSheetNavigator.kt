package com.asfoundation.wallet.backup.bottomSheet
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.appcoins.wallet.core.arch.data.Navigator
import com.asf.wallet.R
import com.asfoundation.wallet.backup.BackupSaveOptionsComposeFragment.Companion.SAVE_PLACE_KEY
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class BackupSaveOnDeviceBottomSheetNavigator @Inject constructor(private val fragment: Fragment,
) : Navigator {

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }
  fun navigateToSuccessScreen(navController: NavController) {
    val bundle = Bundle()
    bundle.putBoolean(SAVE_PLACE_KEY, true)
    navController.navigate(R.id.backup_wallet_success_screen, args = bundle)
  }

}