package com.asfoundation.wallet.billing.address

import com.appcoins.wallet.core.utils.android_common.extensions.component6
import com.appcoins.wallet.core.utils.android_common.extensions.guardLet
import com.appcoins.wallet.sharedpreferences.SecurePreferencesDataSource
import javax.inject.Inject

class BillingAddressRepository @Inject constructor(
  private val securePreferencesDataSource: SecurePreferencesDataSource
) {

  companion object {
    private const val BILLING_ADDRESS_PREFIX = "billing_address"
  }

  fun saveBillingAddress(billingAddressModel: BillingAddressModel) {
    securePreferencesDataSource.saveStrings(
      Pair("${BILLING_ADDRESS_PREFIX}.address", billingAddressModel.address),
      Pair("${BILLING_ADDRESS_PREFIX}.city", billingAddressModel.city),
      Pair("${BILLING_ADDRESS_PREFIX}.zipcode", billingAddressModel.zipcode),
      Pair("${BILLING_ADDRESS_PREFIX}.state", billingAddressModel.state),
      Pair("${BILLING_ADDRESS_PREFIX}.country", billingAddressModel.country),
      Pair("${BILLING_ADDRESS_PREFIX}.number", billingAddressModel.number)
    )
  }

  fun forgetBillingAddress() {
    securePreferencesDataSource.remove(
      "${BILLING_ADDRESS_PREFIX}.address",
      "${BILLING_ADDRESS_PREFIX}.city",
      "${BILLING_ADDRESS_PREFIX}.zipcode",
      "${BILLING_ADDRESS_PREFIX}.state",
      "${BILLING_ADDRESS_PREFIX}.country",
      "${BILLING_ADDRESS_PREFIX}.number"
    )
  }

  /**
   * Retrieves a saved billing address. Note that this does not store whether the card should be
   * saved or not, this field will always be returned as false.
   *
   * @return BillingAddressModel or null if any field could not be retrieved
   */
  fun retrieveBillingAddress(): BillingAddressModel? {
    val (address, city, zipcode, state, country, number) = guardLet(
      securePreferencesDataSource.getString("${BILLING_ADDRESS_PREFIX}.address", null),
      securePreferencesDataSource.getString("${BILLING_ADDRESS_PREFIX}.city", null),
      securePreferencesDataSource.getString("${BILLING_ADDRESS_PREFIX}.zipcode", null),
      securePreferencesDataSource.getString("${BILLING_ADDRESS_PREFIX}.state", null),
      securePreferencesDataSource.getString("${BILLING_ADDRESS_PREFIX}.country", null),
      securePreferencesDataSource.getString("${BILLING_ADDRESS_PREFIX}.number", null)
    ) {
      return null
    }
    return BillingAddressModel(address, city, zipcode, state, country, number, false)
  }
}


