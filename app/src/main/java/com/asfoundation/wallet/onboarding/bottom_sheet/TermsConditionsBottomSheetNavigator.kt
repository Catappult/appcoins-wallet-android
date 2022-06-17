package com.asfoundation.wallet.onboarding.bottom_sheet

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.asf.wallet.R
import com.asfoundation.wallet.base.Navigator
import com.asfoundation.wallet.my_wallets.create_wallet.CreateWalletDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class TermsConditionsBottomSheetNavigator @Inject constructor(private val fragment: Fragment) :
  Navigator {

  fun navigateToBrowser(uri: Uri) {
    try {
      val launchBrowser = Intent(Intent.ACTION_VIEW, uri)
      launchBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      fragment.requireContext()
        .startActivity(launchBrowser)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      Toast.makeText(fragment.requireContext(), R.string.unknown_error, Toast.LENGTH_SHORT)
        .show()
    }
  }

  fun navigateBack() {
    (fragment as BottomSheetDialogFragment).dismiss()
  }

  fun navigateToCreateWalletDialog() {
    navigateBack()
    CreateWalletDialogFragment.newInstance(needsWalletCreation = true)
      .show(fragment.parentFragmentManager, "CreateWalletDialogFragment")
  }
}