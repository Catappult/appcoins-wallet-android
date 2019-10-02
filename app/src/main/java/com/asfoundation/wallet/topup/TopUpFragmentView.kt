package com.asfoundation.wallet.topup

import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import java.math.BigDecimal

interface TopUpFragmentView {

  fun getChangeCurrencyClick(): Observable<Any>

  fun getEditTextChanges(): Observable<TopUpData>

  fun getPaymentMethodClick(): Observable<String>

  fun getNextClick(): Observable<TopUpData>

  fun setupUiElements(paymentMethods: List<PaymentMethodData>, localCurrency: LocalCurrency)

  fun setConversionValue(topUpData: TopUpData)

  fun switchCurrencyData()

  fun setNextButtonState(enabled: Boolean)

  fun showLoading()

  fun showPaymentDetailsForm()

  fun showPaymentMethods()

  fun rotateChangeCurrencyButton()

  fun toggleSwitchCurrencyOn()

  fun toggleSwitchCurrencyOff()

  fun hideBonus()

  fun showBonus(bonus: BigDecimal, currency: String)

  fun showMaxValueWarning(value: String)

  fun showMinValueWarning(value: String)

  fun hideValueInputWarning()

  fun changeMainValueColor(isValid: Boolean)

  fun setupDefaultValueChips(values: List<FiatValue>)

  fun changeMainValueText(value: String)

  fun unselectChips()

  fun selectChip(index: Int)

  fun getChipsClick(): Observable<Int>

  fun getSelectedCurrency(): String

  fun getSelectedChip(): Int

  fun initialInputSetup(preselectedChip: Int, preselectedChipValue: String)

  fun paymentMethodsFocusRequest()

  fun disableSwapCurrencyButton()

  fun enableSwapCurrencyButton()
}
