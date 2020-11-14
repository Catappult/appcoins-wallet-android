package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.payments.carrier.status.CarrierPaymentFragment

class CarrierConfirmNavigator(private val fragmentManager: FragmentManager) {

  fun navigateBack() {
    fragmentManager.popBackStack()
  }

  fun navigateToPayment(domain: String, transactionData: String,
                        transactionType: String, paymentUrl: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CarrierPaymentFragment.newInstance(domain, transactionData, transactionType,
                paymentUrl))
        .addToBackStack(null)
        .commit()
  }

}