package com.asfoundation.wallet.topup.address

import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.topup.TopUpPaymentData
import io.reactivex.Observable

interface BillingAddressTopUpView {

  fun submitClicks(): Observable<BillingAddressModel>

  fun showLoading()

  fun hideLoading()

  fun finishSuccess(billingAddressModel: BillingAddressModel)

  fun initializeView(data: TopUpPaymentData,
                     fiatAmount: String, fiatCurrency: String,
                     shouldStoreCard: Boolean, isStored: Boolean,
                     savedBillingAddress: BillingAddressModel?)
}
