package com.asfoundation.wallet.ui.iab.payments.carrier

import com.appcoins.wallet.billing.carrierbilling.CarrierBillingPreferencesRepository
import dagger.hilt.components.SingletonComponent
import it.czerwinski.android.hilt.annotations.BoundTo
import com.appcoins.wallet.sharedpreferences.SecurePreferencesDataSource
import javax.inject.Inject

@BoundTo(
  supertype = CarrierBillingPreferencesRepository::class,
  component = SingletonComponent::class
)
class SecureCarrierBillingPreferencesRepository @Inject constructor(
  private val securePreferencesDataSource: SecurePreferencesDataSource
) :
  CarrierBillingPreferencesRepository {

  companion object {
    private const val PHONE_NUMBER_KEY = "carrier_billing.phone"
  }

  override fun savePhoneNumber(phoneNumber: String) {
    securePreferencesDataSource.saveString(PHONE_NUMBER_KEY, phoneNumber)
  }

  override fun forgetPhoneNumber() {
    securePreferencesDataSource.remove(PHONE_NUMBER_KEY)
  }

  override fun retrievePhoneNumber(): String? {
    return securePreferencesDataSource.getString(PHONE_NUMBER_KEY, null)
  }

}