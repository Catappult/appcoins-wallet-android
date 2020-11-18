package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.payments.carrier.confirm.CarrierConfirmFragment
import com.asfoundation.wallet.ui.iab.payments.common.error.IabErrorFragment
import java.math.BigDecimal


class CarrierVerifyNavigator(private val fragmentManager: FragmentManager,
                             private val iabActivity: IabActivity) {

  fun navigateBack() {
    fragmentManager.popBackStack()
  }

  fun navigateToConfirm(uid: String, domain: String, transactionData: String,
                        transactionType: String,
                        paymentUrl: String?, currency: String?, amount: BigDecimal,
                        appcAmount: BigDecimal, bonus: BigDecimal?, skuDescription: String,
                        feeFiatAmount: BigDecimal,
                        carrierName: String, carrierImage: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CarrierConfirmFragment.newInstance(uid, domain, transactionData, transactionType,
                paymentUrl, currency, amount, appcAmount, bonus, skuDescription, feeFiatAmount,
                carrierName, carrierImage))
        .addToBackStack(null)
        .commit()
  }

  fun navigateToError(message: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            IabErrorFragment.newInstance(message, CarrierVerifyFragment.BACKSTACK_NAME))
        .addToBackStack(null)
        .commit()
  }

  fun finishActivityWithError() {
    iabActivity.finishWithError()
  }

  fun navigateToWalletValidation(@StringRes messageStringRes: Int) {
    iabActivity.showWalletValidation(messageStringRes)
  }
}