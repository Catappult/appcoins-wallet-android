package com.asfoundation.wallet.ui.settings.change_currency.bottom_sheet

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.settings.entry.SettingsFragment

class ChooseCurrencyBottomSheetNavigator(private val fragmentManager: FragmentManager) {

  fun navigateBackToSettings() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            SettingsFragment.newInstance())
        .commit()
  }
}