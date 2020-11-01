package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import androidx.fragment.app.FragmentManager

class CarrierConfirmNavigator(private val fragmentManager: FragmentManager) {

  fun navigateBack() {
    fragmentManager.popBackStack()
  }

  fun navigateToWebview() {

  }
}