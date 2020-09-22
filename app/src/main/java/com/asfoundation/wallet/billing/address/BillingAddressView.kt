package com.asfoundation.wallet.billing.address

import io.reactivex.Observable

interface BillingAddressView {

  fun backClicks(): Observable<Any>

  fun submitClicks(): Observable<BillingAddressModel>

  fun showMoreMethods()

  fun showLoading()

  fun hideLoading()

}
