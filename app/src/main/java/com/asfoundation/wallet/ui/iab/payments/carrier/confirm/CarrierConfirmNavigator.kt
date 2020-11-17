package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.payments.carrier.status.CarrierPaymentFragment
import java.math.BigDecimal

class CarrierConfirmNavigator(private val fragmentManager: FragmentManager) {

  fun navigateBack() {
    fragmentManager.popBackStack()
  }

  fun navigateToPayment(domain: String, transactionData: String,
                        transactionType: String, paymentUrl: String, currency: String,
                        bonusAmount: BigDecimal) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CarrierPaymentFragment.newInstance(domain, transactionData, transactionType,
                paymentUrl, currency, bonusAmount))
        .addToBackStack(null)
        .commit()
  }

}