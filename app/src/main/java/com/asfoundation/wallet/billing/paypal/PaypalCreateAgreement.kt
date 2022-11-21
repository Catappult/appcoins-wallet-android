package com.asfoundation.wallet.billing.paypal

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
