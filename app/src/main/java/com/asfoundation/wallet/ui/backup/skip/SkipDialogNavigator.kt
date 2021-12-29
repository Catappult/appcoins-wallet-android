package com.asfoundation.wallet.ui.backup.skip

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.home.HomeFragment

class SkipDialogNavigator(val fragment: SkipDialogFragment,
                          private val fragmentManager: FragmentManager) {

  fun navigateBack() {
    fragment.dismiss()
  }

  fun navigateHomeScreen() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container, HomeFragment.newInstance())
        .commit()
    fragment.dismiss()
  }
}