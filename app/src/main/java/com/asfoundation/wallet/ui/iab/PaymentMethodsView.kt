package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.PaymentType
import io.reactivex.Observable
import java.math.BigDecimal

interface PaymentMethodsView {
  fun showPaymentMethods(paymentMethods: MutableList<PaymentMethod>, fiatValue: FiatValue,
                         currency: String, paymentMethodId: String, fiatAmount: String,
                         appcAmount: String, appcEnabled: Boolean, creditsEnabled: Boolean)

  fun showPreSelectedPaymentMethod(paymentMethod: PaymentMethod, fiatValue: FiatValue,
                                   currency: String, fiatAmount: String,
                                   appcAmount: String, isBonusActive: Boolean)

  fun showError(message: Int)

  fun showItemAlreadyOwnedError()

  fun finish(bundle: Bundle)

  fun showPaymentsSkeletonLoading()

  fun showSkeletonLoading()

  fun showProgressBarLoading()

  fun hideLoading()

  fun getCancelClick(): Observable<PaymentMethod>

  fun close(bundle: Bundle)

  fun errorDismisses(): Observable<Boolean>

  fun setupUiCompleted(): Observable<Boolean>

  fun showProcessingLoadingDialog()

  fun getBuyClick(): Observable<PaymentMethod>

  fun showPaypal(gamificationLevel: Int)

  fun showAdyen(fiatValue: FiatValue,
                paymentType: PaymentType,
                iconUrl: String?, gamificationLevel: Int)

  fun showCreditCard(gamificationLevel: Int)

  fun showAppCoins(gamificationLevel: Int)

  fun showCredits(gamificationLevel: Int)

  fun showShareLink(selectedPaymentMethod: String)

  fun getPaymentSelection(): Observable<String>

  fun getMorePaymentMethodsClicks(): Observable<PaymentMethod>

  fun showLocalPayment(selectedPaymentMethod: String, iconUrl: String, label: String,
                       gamificationLevel: Int)

  fun setBonus(bonus: BigDecimal, currency: String)

  fun onBackPressed(): Observable<Boolean>

  fun showNext()

  fun showBuy()

  fun showMergedAppcoins(gamificationLevel: Int, disabledReasonAppc: Int,
                         disabledReasonCredits: Int)

  fun lockRotation()

  fun showEarnAppcoins()

  fun showBonus()

  fun hideBonus()

  fun replaceBonus()

  fun removeBonus()

  fun getSupportLogoClicks(): Observable<Any>

  fun getSupportIconClicks(): Observable<Any>

  enum class SelectedPaymentMethod {
    PAYPAL, CREDIT_CARD, APPC, APPC_CREDITS, MERGED_APPC, SHARE_LINK, LOCAL_PAYMENTS, EARN_APPC,
    ERROR

  }

  enum class PaymentMethodId(val id: String) {
    PAYPAL("paypal"),
    APPC("appcoins"),
    APPC_CREDITS("appcoins_credits"),
    MERGED_APPC("merged_appcoins"),
    CREDIT_CARD("credit_card"),
    ASK_FRIEND("ask_friend")
  }
}
