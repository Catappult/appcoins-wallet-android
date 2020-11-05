package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.payments.carrier.confirm.CarrierConfirmFragment
import com.asfoundation.wallet.ui.iab.payments.common.error.IabErrorFragment
import java.math.BigDecimal


class CarrierVerifyNavigator(private val fragmentManager: FragmentManager) {

  fun navigateBack() {
    fragmentManager.popBackStack()
  }

  fun navigateToConfirm(domain: String, paymentUrl: String?,
                        currency: String?, amount: BigDecimal, appcAmount: BigDecimal,
                        bonus: BigDecimal?, skuDescription: String, feeFiatAmount: BigDecimal,
                        carrierName: String, carrierImage: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CarrierConfirmFragment.newInstance(domain, paymentUrl,
                currency, amount, appcAmount, bonus, skuDescription, feeFiatAmount, carrierName,
                carrierImage))
        .addToBackStack(null)
        .commit()
  }

  fun navigateToError() {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            IabErrorFragment.newInstance(R.string.purchase_carrier_error,
                CarrierVerifyFragment.BACKSTACK_NAME))
        .addToBackStack(null)
        .commit()
  }
}