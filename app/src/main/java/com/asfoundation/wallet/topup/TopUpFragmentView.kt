package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.ui.iab.PaymentMethod
import io.reactivex.Observable
import java.math.BigDecimal

interface TopUpFragmentView {

  fun getChangeCurrencyClick(): Observable<Any>
  fun getEditTextChanges(): Observable<TopUpData>
  fun getPaymentMethodClick(): Observable<String>
  fun getNextClick(): Observable<TopUpData>
  fun setupPaymentMethods(paymentMethods: List<PaymentMethod>)
  fun setupCurrency(localCurrency: LocalCurrency)
  fun setConversionValue(topUpData: TopUpData)
  fun switchCurrencyData()
  fun setNextButtonState(enabled: Boolean)
  fun rotateChangeCurrencyButton()
  fun toggleSwitchCurrencyOn()
  fun toggleSwitchCurrencyOff()
  fun hideBonus()
  fun hideBonusAndSkeletons()
  fun showBonus(bonus: BigDecimal, currency: String)
  fun showMaxValueWarning(value: String)
  fun showMinValueWarning(value: String)
  fun hideValueInputWarning()
  fun changeMainValueColor(isValid: Boolean)
  fun changeMainValueText(value: String)
  fun getSelectedCurrency(): String
  fun paymentMethodsFocusRequest()
  fun disableSwapCurrencyButton()
  fun enableSwapCurrencyButton()
  fun showNoNetworkError()
  fun showRetryAnimation()
  fun retryClick(): Observable<Any>
  fun getValuesClicks(): Observable<FiatValue>
  fun setValuesAdapter(values: List<FiatValue>)
  fun showValuesAdapter()
  fun hideValuesAdapter()
  fun getKeyboardEvents(): Observable<Boolean>
  fun setDefaultAmountValue(amount: String)
  fun removeBonus()
  fun showSkeletons()
  fun showBonusSkeletons()
  fun hidePaymentMethods()
}
