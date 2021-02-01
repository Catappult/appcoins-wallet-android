package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.payments.carrier.status.CarrierPaymentFragment
import java.math.BigDecimal

class CarrierFeeNavigator(private val iabActivity: IabActivity,
                          private val fragmentManager: FragmentManager) {

  fun navigateToPaymentMethods() = iabActivity.showPaymentMethodsView()

  fun navigateToPayment(domain: String, transactionData: String, transactionType: String,
                        skuId: String?, paymentUrl: String, appcAmount: BigDecimal,
                        currency: String, bonusAmount: BigDecimal?, phoneNumber: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CarrierPaymentFragment.newInstance(domain, transactionData, transactionType, skuId,
                paymentUrl, appcAmount, currency, bonusAmount, phoneNumber))
        .addToBackStack(null)
        .commit()
  }

}