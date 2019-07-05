package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import io.reactivex.Observable
import java.io.IOException
import java.math.BigDecimal

interface PaymentMethodsView {
  fun showPaymentMethods(paymentMethods: MutableList<PaymentMethod>, fiatValue: FiatValue,
                         isDonation: Boolean, currency: String)

  fun showError(message: Int)
  @Throws(IOException::class)
  fun finish(purchase: Purchase)

  fun showLoading()
  fun hideLoading()
  fun getCancelClick(): Observable<Any>
  fun close(bundle: Bundle)
  fun errorDismisses(): Observable<Any>
  fun setupUiCompleted(): Observable<Boolean>
  fun showProcessingLoadingDialog()
  fun setWalletAddress(address: String)
  fun getBuyClick(): Observable<String>
  fun showPaypal()
  fun showCreditCard()
  fun showAppCoins()
  fun showCredits()
  fun showShareLink(selectedPaymentMethod: String)
  fun hideBonus()
  fun showBonus()
  fun getPaymentSelection(): Observable<String>
  fun showLocalPayment(selectedPaymentMethod: String)
  fun setBonus(bonus: BigDecimal, currency: String)

  enum class SelectedPaymentMethod {
    PAYPAL, CREDIT_CARD, APPC, APPC_CREDITS, SHARE_LINK, LOCAL_PAYMENTS, ERROR
  }
}
