package com.asfoundation.wallet.billing.address

import io.reactivex.Observable
import java.math.BigDecimal

interface BillingAddressView {

  fun initializeView(bonus: String?, isDonation: Boolean, domain: String,
                     skuDescription: String,
                     appcAmount: BigDecimal, fiatAmount: BigDecimal,
                     fiatCurrency: String,
                     isStored: Boolean, shouldStoreCard: Boolean,
                     savedBillingAddress: BillingAddressModel?)

  fun backClicks(): Observable<Any>

  fun submitClicks(): Observable<BillingAddressModel>

}
