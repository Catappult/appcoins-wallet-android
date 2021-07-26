package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.PaymentType
import io.reactivex.Observable
import java.math.BigDecimal

interface PaymentMethodsView {
  fun showPaymentMethods(paymentMethods: MutableList<PaymentMethod>,
                         currency: String, paymentMethodId: String, fiatAmount: String,
                         appcAmount: String, appcEnabled: Boolean, creditsEnabled: Boolean)

  fun showPreSelectedPaymentMethod(paymentMethod: PaymentMethod, currency: String,
                                   fiatAmount: String, appcAmount: String, isBonusActive: Boolean)

  fun showError(message: Int)

  fun showItemAlreadyOwnedError()

  fun finish(bundle: Bundle)

  fun showPaymentsSkeletonLoading()

  fun showSkeletonLoading()

  fun showProgressBarLoading()

  fun hideLoading()

  fun getCancelClick(): Observable<Any>

  fun close(bundle: Bundle)

  fun errorDismisses(): Observable<Boolean>

  fun setupUiCompleted(): Observable<Boolean>

  fun showProcessingLoadingDialog()

  fun getBuyClick(): Observable<Any>

  fun showCarrierBilling(fiatValue: FiatValue, isPreselected: Boolean)

  fun showPaypal(gamificationLevel: Int, fiatValue: FiatValue)

  fun showAdyen(fiatAmount: BigDecimal,
                fiatCurrency: String,
                paymentType: PaymentType,
                iconUrl: String?, gamificationLevel: Int)

  fun showCreditCard(gamificationLevel: Int, fiatValue: FiatValue)

  fun showAppCoins(gamificationLevel: Int)

  fun showCredits(gamificationLevel: Int)

  fun showShareLink(selectedPaymentMethod: String)

  fun getPaymentSelection(): Observable<String>

  fun getMorePaymentMethodsClicks(): Observable<Any>

  fun showLocalPayment(selectedPaymentMethod: String, iconUrl: String, label: String,
                       async: Boolean, fiatAmount: String, fiatCurrency: String,
                       gamificationLevel: Int)

  fun setBonus(bonus: BigDecimal, currency: String)

  fun onBackPressed(): Observable<Boolean>

  fun showNext()

  fun showBuy()

  fun showMergedAppcoins(gamificationLevel: Int, fiatValue: FiatValue)

  fun lockRotation()

  fun showEarnAppcoins()

  fun showBonus()

  fun hideBonus()

  fun replaceBonus()

  fun removeBonus()

  fun getSupportLogoClicks(): Observable<Any>

  fun getSupportIconClicks(): Observable<Any>

  fun showAuthenticationActivity()

  fun onAuthenticationResult(): Observable<Boolean>

  fun getSelectedPaymentMethod(hasPreSelectedPaymentMethod: Boolean): PaymentMethod

  fun updateProductName()

  fun getTopupClicks(): Observable<String>

  fun showTopupFlow()

  enum class SelectedPaymentMethod {
    PAYPAL, CREDIT_CARD, APPC, APPC_CREDITS, MERGED_APPC, SHARE_LINK, LOCAL_PAYMENTS, EARN_APPC,
    CARRIER_BILLING, ERROR
  }

  enum class PaymentMethodId(val id: String) {
    PAYPAL("paypal"),
    APPC("appcoins"),
    APPC_CREDITS("appcoins_credits"),
    MERGED_APPC("merged_appcoins"),
    CREDIT_CARD("credit_card"),
    CARRIER_BILLING("onebip"),
    ASK_FRIEND("ask_friend")

  }
}
