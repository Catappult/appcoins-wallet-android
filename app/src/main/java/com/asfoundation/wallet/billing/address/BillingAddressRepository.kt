package com.asfoundation.wallet.billing.address

import com.asfoundation.wallet.util.component6
import com.asfoundation.wallet.util.guardLet
import repository.SecureSharedPreferences
import javax.inject.Inject

class BillingAddressRepository @Inject constructor(
    private val secureSharedPreferences: SecureSharedPreferences) {

  companion object {
    private const val BILLING_ADDRESS_PREFIX = "billing_address"
  }

  fun saveBillingAddress(billingAddressModel: BillingAddressModel) {
    secureSharedPreferences.saveStrings(
        Pair("${BILLING_ADDRESS_PREFIX}.address", billingAddressModel.address),
        Pair("${BILLING_ADDRESS_PREFIX}.city", billingAddressModel.city),
        Pair("${BILLING_ADDRESS_PREFIX}.zipcode", billingAddressModel.zipcode),
        Pair("${BILLING_ADDRESS_PREFIX}.state", billingAddressModel.state),
        Pair("${BILLING_ADDRESS_PREFIX}.country", billingAddressModel.country),
        Pair("${BILLING_ADDRESS_PREFIX}.number", billingAddressModel.number)
    )
  }

  fun forgetBillingAddress() {
    secureSharedPreferences.remove("${BILLING_ADDRESS_PREFIX}.address",
        "${BILLING_ADDRESS_PREFIX}.city",
        "${BILLING_ADDRESS_PREFIX}.zipcode",
        "${BILLING_ADDRESS_PREFIX}.state",
        "${BILLING_ADDRESS_PREFIX}.country",
        "${BILLING_ADDRESS_PREFIX}.number")
  }

  /**
   * Retrieves a saved billing address. Note that this does not store whether the card should be
   * saved or not, this field will always be returned as false.
   *
   * @return BillingAddressModel or null if any field could not be retrieved
   */
  fun retrieveBillingAddress(): BillingAddressModel? {
    val (address, city, zipcode, state, country, number) = guardLet(
        secureSharedPreferences.getString("${BILLING_ADDRESS_PREFIX}.address", null),
        secureSharedPreferences.getString("${BILLING_ADDRESS_PREFIX}.city", null),
        secureSharedPreferences.getString("${BILLING_ADDRESS_PREFIX}.zipcode", null),
        secureSharedPreferences.getString("${BILLING_ADDRESS_PREFIX}.state", null),
        secureSharedPreferences.getString("${BILLING_ADDRESS_PREFIX}.country", null),
        secureSharedPreferences.getString("${BILLING_ADDRESS_PREFIX}.number", null)
    ) {
      return null
    }
    return BillingAddressModel(address, city, zipcode, state, country, number, false)
  }
}


