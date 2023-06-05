package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


data class HeaderPaypalMock(
  @SerializedName("mock_application_codes") val mock_application_codes: String,
) {
  fun toJson(): String{
    return Gson().toJson(this)
  }
}

// Codes for mocking Paypal, only works in dev
enum class MockCodes(code: String){
  INSTRUMENT_DECLINED("INSTRUMENT_DECLINED "),
  PAYER_ACCOUNT_RESTRICTED("PAYER_ACCOUNT_RESTRICTED"),
  PAYER_CANNOT_PAY("PAYER_CANNOT_PAY"),
  REDIRECT_PAYER_FOR_ALTERNATE_FUNDING("REDIRECT_PAYER_FOR_ALTERNATE_FUNDING"),
  TRANSACTION_REFUSED("TRANSACTION_REFUSED")
}
