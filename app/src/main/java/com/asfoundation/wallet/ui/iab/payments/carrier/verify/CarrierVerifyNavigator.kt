package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.asf.wallet.R
import com.asfoundation.wallet.ui.iab.IabActivity
import com.asfoundation.wallet.ui.iab.payments.carrier.confirm.CarrierFeeFragment
import com.asfoundation.wallet.ui.iab.payments.common.error.IabErrorFragment
import java.math.BigDecimal
import javax.inject.Inject


class CarrierVerifyNavigator @Inject constructor(private val fragmentManager: FragmentManager,
                             fragment: Fragment) {

  private val iabActivity = fragment.activity as IabActivity

  fun navigateBack() = fragmentManager.popBackStack()

  fun navigateToFee(uid: String, domain: String, transactionData: String,
                    transactionType: String, paymentUrl: String, currency: String,
                    amount: BigDecimal, appcAmount: BigDecimal, bonus: BigDecimal?,
                    skuDescription: String, skuId: String?, feeFiatAmount: BigDecimal,
                    carrierName: String, carrierImage: String, phoneNumber: String) {
    fragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CarrierFeeFragment.newInstance(uid, domain, transactionData, transactionType,
                paymentUrl, currency, amount, appcAmount, bonus, skuDescription, skuId,
                feeFiatAmount, carrierName, carrierImage, phoneNumber))
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

  fun finishActivityWithError() = iabActivity.finishWithError()

  fun navigateToVerification() = iabActivity.showVerification(false)
}