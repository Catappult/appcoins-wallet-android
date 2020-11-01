package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import androidx.fragment.app.FragmentManager


class CarrierVerifyNavigator(private val fragmentManager: FragmentManager) {

  fun navigateBack() {
    fragmentManager.popBackStack()
  }

  fun navigateToConfirm() {

  }

  fun navigateToError() {

  }
}