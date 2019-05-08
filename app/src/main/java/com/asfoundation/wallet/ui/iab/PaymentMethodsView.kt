package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import io.reactivex.Observable
import java.io.IOException
import java.math.BigDecimal

interface PaymentMethodsView {
  fun showPaymentMethods(paymentMethods: MutableList<PaymentMethod>,
                         availablePaymentMethods: MutableList<PaymentMethod>, fiatValue: FiatValue,
                         isDonation: Boolean, currency: String)

  fun showError()
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
  fun getBuyClick(): Observable<SelectedPaymentMethod>
  fun showPaypal()
  fun showCreditCard()
  fun showAppCoins()
  fun showCredits()
  fun showShareLink()
  fun hideBonus()
  fun showBonus(bonus: BigDecimal, currency: String)
  fun getPaymentSelection(): Observable<SelectedPaymentMethod>

  enum class SelectedPaymentMethod {
    PAYPAL, CREDIT_CARD, APPC, APPC_CREDITS, SHARE_LINK
  }
}
