package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.asfoundation.wallet.billing.adyen.PaymentType
import io.reactivex.Observable
import java.math.BigDecimal

interface PaymentMethodsView {
  fun showPaymentMethods(paymentMethods: MutableList<PaymentMethod>, fiatValue: FiatValue,
                         currency: String, paymentMethodId: String)

  fun showPreSelectedPaymentMethod(paymentMethod: PaymentMethod, fiatValue: FiatValue,
                                   isDonation: Boolean, currency: String)

  fun showError(message: Int)
  fun showItemAlreadyOwnedError()
  fun finish(bundle: Bundle)
  fun showLoading()
  fun hideLoading()
  fun getCancelClick(): Observable<Any>
  fun close(bundle: Bundle)
  fun errorDismisses(): Observable<Boolean>
  fun setupUiCompleted(): Observable<Boolean>
  fun showProcessingLoadingDialog()
  fun getBuyClick(): Observable<PaymentMethod>
  fun showPaypal()
  fun showAdyen(fiatValue: FiatValue, paymentType: PaymentType, iconUrl: String?)
  fun showCreditCard()
  fun showAppCoins()
  fun showCredits()
  fun showShareLink(selectedPaymentMethod: String)
  fun getPaymentSelection(): Observable<String>
  fun getMorePaymentMethodsClicks(): Observable<Any>
  fun showLocalPayment(selectedPaymentMethod: String, iconUrl: String, label: String)
  fun setBonus(bonus: BigDecimal, currency: String)
  fun onBackPressed(): Observable<Boolean>
  fun showNext()
  fun showBuy()
  fun showMergedAppcoins()
  fun lockRotation()
  fun showEarnAppcoins()
  fun showBonus()
  fun replaceBonus()
  fun showWalletBlocked()

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
