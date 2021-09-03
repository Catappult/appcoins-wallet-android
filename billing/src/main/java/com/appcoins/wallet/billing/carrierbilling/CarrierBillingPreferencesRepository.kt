package com.appcoins.wallet.billing.carrierbilling

interface CarrierBillingPreferencesRepository {

  fun savePhoneNumber(phoneNumber: String)

  fun forgetPhoneNumber()

  fun retrievePhoneNumber(): String?

}