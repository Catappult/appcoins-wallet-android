package com.asfoundation.wallet.billing.paypal.models

import com.asfoundation.wallet.billing.paypal.repository.PaypalV2CreateAgreementResponse

data class PaypalCreateAgreement(
  val uid: String
) {

  companion object {
    fun map(response: PaypalV2CreateAgreementResponse): PaypalCreateAgreement {
      return PaypalCreateAgreement(
        response.uid,
      )
    }
  }

}
