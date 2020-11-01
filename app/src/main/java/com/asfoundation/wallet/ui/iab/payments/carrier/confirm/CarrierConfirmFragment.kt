package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.fragment_carrier_confirm.*
import kotlinx.android.synthetic.main.fragment_carrier_verify_phone.payment_methods_header
import kotlinx.android.synthetic.main.fragment_carrier_verify_phone.purchase_bonus
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class CarrierConfirmFragment : DaggerFragment(), CarrierConfirmView {

  private val formatter = CurrencyFormatUtils()

  @Inject
  lateinit var presenter: CarrierConfirmPresenter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_carrier_confirm, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    setupUi()
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  private fun setupUi() {
    cancel_button.setText(R.string.back_button)
    cancel_button.visibility = View.VISIBLE

    buy_button.setText(R.string.action_next)
    buy_button.visibility = View.VISIBLE
    buy_button.isEnabled = false
  }

  override fun initializeView(appName: String, appIcon: Drawable,
                              currency: String, fiatAmount: BigDecimal,
                              appcAmount: BigDecimal, skuDescription: String,
                              bonusAmount: BigDecimal, carrierName: String, carrierImage: String,
                              carrierFee: BigDecimal) {
    buy_button.isEnabled = true
    payment_methods_header.setTitle(appName)
    payment_methods_header.setIcon(appIcon)
    payment_methods_header.setDescription(skuDescription)
    payment_methods_header.hidePrice()
    payment_methods_header.hideSkeleton()

    val fiat = "${formatter.formatCurrency(fiatAmount, WalletCurrency.FIAT)} $currency"
    val appc = "${formatter.formatCurrency(appcAmount,
        WalletCurrency.APPCOINS)} ${WalletCurrency.APPCOINS.symbol}"
    fiat_price.text = fiat
    appc_price.text = appc

    purchase_bonus.setPurchaseBonusHeaderValue(bonusAmount, mapCurrencyCodeToSymbol(currency))
    purchase_bonus.hideSkeleton()
  }

  private fun mapCurrencyCodeToSymbol(currencyCode: String): String {
    return if (currencyCode.equals("APPC", ignoreCase = true))
      currencyCode
    else
      Currency.getInstance(currencyCode)
          .symbol
  }

  override fun backEvent(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun nextClickEvent(): Observable<Any> {
    return RxView.clicks(buy_button)
  }
}