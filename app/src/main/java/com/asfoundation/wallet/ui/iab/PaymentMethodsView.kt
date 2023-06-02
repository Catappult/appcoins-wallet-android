package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import androidx.annotation.StringRes
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import io.reactivex.Observable
import java.math.BigDecimal

interface PaymentMethodsView {
  fun showPaymentMethods(
    paymentMethods: MutableList<PaymentMethod>,
    currency: String, paymentMethodId: String, fiatAmount: String,
    appcAmount: String, appcEnabled: Boolean, creditsEnabled: Boolean,
    frequency: String?, isSubscription: Boolean, showLogoutPaypal: Boolean
  )

  fun showPreSelectedPaymentMethod(
    paymentMethod: PaymentMethod, currency: String,
    fiatAmount: String, appcAmount: String, isBonusActive: Boolean,
    frequency: String?, isSubscription: Boolean
  )

  fun showError(message: Int)

  fun showItemAlreadyOwnedError()

  fun finish(bundle: Bundle)

  fun showPaymentsSkeletonLoading()

  fun showSkeletonLoading()

  fun showProgressBarLoading()

  fun hideLoading()

  fun getCancelClick(): Observable<Any>

  fun close(bundle: Bundle)

  fun errorDismisses(): Observable<Any>

  fun setupUiCompleted(): Observable<Boolean>

  fun showProcessingLoadingDialog()

  fun getBuyClick(): Observable<Any>

  fun showCarrierBilling(fiatValue: FiatValue, isPreselected: Boolean)

  fun showPaypal(
    gamificationLevel: Int, fiatValue: FiatValue, frequency: String?,
    isSubscription: Boolean
  )

  fun showPaypalV2(
    gamificationLevel: Int, fiatValue: FiatValue, frequency: String?,
    isSubscription: Boolean
  )

  fun showAdyen(
    fiatAmount: BigDecimal,
    fiatCurrency: String,
    paymentType: PaymentType,
    iconUrl: String?, gamificationLevel: Int, frequency: String?,
    isSubscription: Boolean
  )

  fun showCreditCard(
    gamificationLevel: Int, fiatValue: FiatValue, frequency: String?,
    isSubscription: Boolean
  )

  fun showAppCoins(gamificationLevel: Int, transaction: TransactionBuilder)

  fun showCredits(gamificationLevel: Int, transaction: TransactionBuilder)

  fun showShareLink(selectedPaymentMethod: String)

  fun getPaymentSelection(): Observable<String>

  fun getMorePaymentMethodsClicks(): Observable<Any>

  fun showLocalPayment(
    selectedPaymentMethod: String, iconUrl: String, label: String,
    async: Boolean, fiatAmount: String, fiatCurrency: String,
    gamificationLevel: Int
  )

  fun setPurchaseBonus(bonus: BigDecimal, currency: String, @StringRes bonusText: Int)

  fun onBackPressed(): Observable<Any>

  fun showNext()

  fun showBuy()

  fun showMergedAppcoins(
    gamificationLevel: Int, fiatValue: FiatValue,
    transaction: TransactionBuilder, frequency: String?,
    isSubscription: Boolean
  )

  fun showSubscribe()

  fun lockRotation()

  fun showEarnAppcoins()

  fun showBonus(@StringRes bonusText: Int)

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
    PAYPAL, PAYPAL_V2, CREDIT_CARD, APPC, APPC_CREDITS, MERGED_APPC, SHARE_LINK, LOCAL_PAYMENTS,
    EARN_APPC, CARRIER_BILLING, ERROR
  }

  enum class PaymentMethodId(val id: String) {
    PAYPAL("paypal"),
    PAYPAL_V2("paypal_v2"),
    APPC("appcoins"),
    APPC_CREDITS("appcoins_credits"),
    MERGED_APPC("merged_appcoins"),
    CREDIT_CARD("credit_card"),
    CARRIER_BILLING("onebip"),
    ASK_FRIEND("ask_friend")
  }
}
