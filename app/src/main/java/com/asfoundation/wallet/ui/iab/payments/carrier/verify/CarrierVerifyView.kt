package com.asfoundation.wallet.ui.iab.payments.carrier.verify

import android.graphics.drawable.Drawable
import io.reactivex.Observable
import java.math.BigDecimal

interface CarrierVerifyView {

  fun initializeView(currency: String, fiatAmount: BigDecimal, appcAmount: BigDecimal,
                     skuDescription: String, bonusAmount: BigDecimal?, preselected: Boolean)

  fun backEvent(): Observable<Any>

  fun nextClickEvent(): Observable<String>

  fun phoneNumberChangeEvent(): Observable<Pair<String, Boolean>>

  fun otherPaymentMethodsEvent(): Observable<Any>

  fun setLoading()

  fun showInvalidPhoneNumberError()

  fun removePhoneNumberFieldError()

  fun setNextButtonEnabled(enabled: Boolean)

  fun lockRotation()

  fun unlockRotation()

  fun setAppDetails(appName: String, icon: Drawable)

  fun filterCountries(countryListString: String, defaultCountry: String?)

  fun showPhoneNumberLayout()

  fun showSavedPhoneNumber(phoneNumber: String)

  fun hideSavedPhoneNumber(clearText: Boolean = false)

  fun changeButtonClick(): Observable<Any>

  fun focusOnPhoneNumber()

}