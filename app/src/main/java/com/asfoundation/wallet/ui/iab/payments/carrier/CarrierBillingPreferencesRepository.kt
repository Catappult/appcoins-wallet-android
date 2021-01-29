package com.asfoundation.wallet.ui.iab.payments.carrier

import com.appcoins.wallet.billing.carrierbilling.CarrierBillingPreferencesRepositoryContract
import com.asfoundation.wallet.repository.SecureSharedPreferences

class CarrierBillingPreferencesRepository(
    private val secureSharedPreferences: SecureSharedPreferences) :
    CarrierBillingPreferencesRepositoryContract {

  companion object {
    private const val PHONE_NUMBER_KEY = "carrier_billing.phone"
  }

  override fun savePhoneNumber(phoneNumber: String) {
    secureSharedPreferences.saveString(PHONE_NUMBER_KEY, phoneNumber)
  }

  override fun forgetPhoneNumber() {
    secureSharedPreferences.remove(PHONE_NUMBER_KEY)
  }

  override fun retrievePhoneNumber(): String? {
    return secureSharedPreferences.getString(PHONE_NUMBER_KEY, null)
  }

}