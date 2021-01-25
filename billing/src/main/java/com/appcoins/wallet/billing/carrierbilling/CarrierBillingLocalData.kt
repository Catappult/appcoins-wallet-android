package com.appcoins.wallet.billing.carrierbilling

interface CarrierBillingLocalData {

  fun savePhoneNumber(phoneNumber: String)

  fun retrievePhoneNumber(): String?

}