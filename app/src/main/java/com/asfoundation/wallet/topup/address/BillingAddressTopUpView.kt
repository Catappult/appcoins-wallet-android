package com.asfoundation.wallet.topup.address

import com.asfoundation.wallet.billing.address.BillingAddressModel
import io.reactivex.Observable

interface BillingAddressTopUpView {

  fun submitClicks(): Observable<BillingAddressModel>

}
