package com.asfoundation.wallet.billing.carrier

import com.appcoins.wallet.billing.carrierbilling.CarrierBillingLocalData
import com.asfoundation.wallet.repository.SecureSharedPreferences

class SecureCarrierBillingLocalData(
    private val secureSharedPreferences: SecureSharedPreferences) : CarrierBillingLocalData {

  companion object {
    private const val CARRIER_BILLING_PREFIX = "carrier_billing"
  }

  override fun savePhoneNumber(phoneNumber: String) {
    secureSharedPreferences.saveString("${CARRIER_BILLING_PREFIX}.phone", phoneNumber)
  }

  override fun forgetPhoneNumber() {
    secureSharedPreferences.remove("${CARRIER_BILLING_PREFIX}.phone")
  }

  override fun retrievePhoneNumber(): String? {
    return secureSharedPreferences.getString("${CARRIER_BILLING_PREFIX}.phone", null)
  }

}