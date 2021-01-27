package com.asfoundation.wallet.ui.iab.payments.carrier

import com.appcoins.wallet.billing.carrierbilling.CarrierBillingLocalData
import com.asfoundation.wallet.repository.SecureSharedPreferences

class SecureCarrierBillingLocalData(
    private val secureSharedPreferences: SecureSharedPreferences) : CarrierBillingLocalData {

  companion object {
    private const val CARRIER_BILLING_PREFIX = "carrier_billing"
    private const val PHONE_SUFFIX = "phone"
  }

  override fun savePhoneNumber(phoneNumber: String) {
    secureSharedPreferences.saveString("$CARRIER_BILLING_PREFIX.$PHONE_SUFFIX", phoneNumber)
  }

  override fun forgetPhoneNumber() {
    secureSharedPreferences.remove("$CARRIER_BILLING_PREFIX.$PHONE_SUFFIX")
  }

  override fun retrievePhoneNumber(): String? {
    return secureSharedPreferences.getString("$CARRIER_BILLING_PREFIX.$PHONE_SUFFIX",
        null)
  }

}