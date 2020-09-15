package com.asfoundation.wallet.billing.address

import com.appcoins.wallet.billing.adyen.PaymentModel
import com.asfoundation.wallet.billing.adyen.AdyenPaymentInteractor
import io.reactivex.Single

class BillingAddressInteractor(private val adyenPaymentInteractor: AdyenPaymentInteractor) {

  fun makePayment(): Single<PaymentModel> {
    TODO()
  }

}