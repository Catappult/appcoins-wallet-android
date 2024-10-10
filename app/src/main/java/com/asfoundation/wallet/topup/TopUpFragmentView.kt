package com.asfoundation.wallet.topup

import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asfoundation.wallet.manage_cards.models.StoredCard
import com.asfoundation.wallet.ui.iab.PaymentMethod
import io.reactivex.Observable
import java.math.BigDecimal

interface TopUpFragmentView {

  fun getEditTextChanges(): Observable<TopUpData>
  fun getPaymentMethodClick(): Observable<PaymentMethod>
  fun getNextClick(): Observable<TopUpData>
  fun setupPaymentMethods(paymentMethods: List<PaymentMethod>, cardsList: List<StoredCard>)
  fun setupCurrency(currency: LocalCurrency)
  fun setConversionValue(topUpData: TopUpData)
  fun switchCurrencyData()
  fun setNextButtonState(enabled: Boolean)
  fun rotateChangeCurrencyButton()
  fun toggleSwitchCurrencyOn()
  fun toggleSwitchCurrencyOff()
  fun hideBonus()
  fun hideBonusAndSkeletons()
  fun showBonus()
  fun showBonus(bonus: BigDecimal, currency: String)
  fun showMaxValueWarning(value: String)
  fun showMinValueWarning(value: String)
  fun hideValueInputWarning()
  fun changeMainValueColor(isValid: Boolean)
  fun changeMainValueText(value: String)
  fun getSelectedCurrencyType(): String
  fun getSelectedCurrency(): LocalCurrency
  fun paymentMethodsFocusRequest()
  fun getCurrentPaymentMethod(): PaymentMethod?
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
  fun showNoMethodsError()
  fun showAsLoading()
  fun hideLoading()
  fun setTopupButton()
  fun setBuyButton()
  fun setNextButton()
  fun showValuesSkeletons()
  fun hideValuesSkeletons()
  fun lockRotation()

  fun changeVisibilityRefundDisclaimer(visible: Boolean)
  fun showFee(visible: Boolean)
}
