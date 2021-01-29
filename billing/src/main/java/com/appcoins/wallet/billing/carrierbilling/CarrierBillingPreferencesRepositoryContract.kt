package com.appcoins.wallet.billing.carrierbilling

interface CarrierBillingPreferencesRepositoryContract {

  fun savePhoneNumber(phoneNumber: String)

  fun forgetPhoneNumber()

  fun retrievePhoneNumber(): String?

}