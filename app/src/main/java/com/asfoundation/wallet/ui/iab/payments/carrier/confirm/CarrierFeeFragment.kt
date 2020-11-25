package com.asfoundation.wallet.ui.iab.payments.carrier.confirm

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannedString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.getStringSpanned
import com.asfoundation.wallet.util.withNoLayoutTransition
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.fragment_carrier_confirm.*
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class CarrierFeeFragment : DaggerFragment(), CarrierFeeView {

  private val formatter = CurrencyFormatUtils()

  @Inject
  lateinit var presenter: CarrierFeePresenter

  lateinit var iabView: IabView

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_carrier_confirm, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present()
  }

  override fun onDestroyView() {
    iabView.enableBack()
    presenter.stop()
    super.onDestroyView()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "CarrierConfirmFragment must be attached to IAB activity" }
    iabView = context
  }

  private fun setupUi() {
    iabView.disableBack()

    cancel_button.setText(R.string.back_button)
    cancel_button.visibility = View.VISIBLE

    buy_button.setText(R.string.action_next)
    buy_button.visibility = View.VISIBLE
    buy_button.isEnabled = false
  }

  override fun initializeView(currency: String, fiatAmount: BigDecimal,
                              appcAmount: BigDecimal, skuDescription: String,
                              bonusAmount: BigDecimal?, carrierName: String,
                              carrierImage: String, carrierFeeFiat: BigDecimal) {
    buy_button.isEnabled = true
    payment_methods_header.setDescription(skuDescription)
    payment_methods_header.hidePrice(
        resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
    payment_methods_header.hideSkeleton()

    val fiat =
        "${formatter.formatCurrency(fiatAmount + carrierFeeFiat, WalletCurrency.FIAT)} $currency"
    val appc = "${formatter.formatCurrency(appcAmount,
        WalletCurrency.APPCOINS)} ${WalletCurrency.APPCOINS.symbol}"
    fiat_price_text.text = fiat
    appc_price_text.text = appc

    val feeString: SpannedString = buildSpannedString {
      color(ResourcesCompat.getColor(resources, R.color.disable_reason, null)) {
        append("${formatter.formatCurrency(carrierFeeFiat, WalletCurrency.FIAT)} $currency")
      }
    }
    fee_title.text =
        context?.getStringSpanned(R.string.carrier_billing_carrier_fees_body, carrierName,
            feeString)

    GlideApp.with(requireContext())
        .load(carrierImage)
        .into(carrier_image)

    purchase_bonus.withNoLayoutTransition {
      if (bonusAmount != null) {
        purchase_bonus.visibility = View.VISIBLE
        purchase_bonus.setPurchaseBonusHeaderValue(bonusAmount, mapCurrencyCodeToSymbol(currency))
        purchase_bonus.hideSkeleton()
      } else {
        purchase_bonus.visibility = View.GONE
      }
    }
  }

  override fun setAppDetails(appName: String, icon: Drawable) {
    payment_methods_header.setTitle(appName)
    payment_methods_header.setIcon(icon)
  }

  private fun mapCurrencyCodeToSymbol(currencyCode: String): String {
    return if (currencyCode.equals("APPC", ignoreCase = true))
      currencyCode
    else
      Currency.getInstance(currencyCode)
          .symbol
  }

  override fun cancelButtonEvent(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun systemBackEvent(): Observable<Any> {
    return iabView.backButtonPress()
  }


  override fun nextClickEvent(): Observable<Any> = RxView.clicks(buy_button)

  companion object {

    internal const val UID_KEY = "uid"
    internal const val DOMAIN_KEY = "domain"
    internal const val PAYMENT_URL_KEY = "payment_url"
    internal const val TRANSACTION_DATA_KEY = "transaction_data"
    internal const val TRANSACTION_TYPE_KEY = "transaction_type"
    internal const val APPC_AMOUNT_KEY = "appc_amount"
    internal const val FIAT_AMOUNT_KEY = "fiat_amount"
    internal const val CURRENCY_KEY = "currency"
    internal const val BONUS_AMOUNT_KEY = "bonus_amount"
    internal const val SKU_DESCRIPTION = "sku_description"
    internal const val SKU_ID = "sku_id"
    internal const val FEE_FIAT_AMOUNT = "fee_fiat_amount"
    internal const val CARRIER_NAME = "carrier_name"
    internal const val CARRIER_IMAGE = "carrier_image"

    @JvmStatic
    fun newInstance(uid: String, domain: String, transactionData: String, transactionType: String,
                    paymentUrl: String?, currency: String?, amount: BigDecimal,
                    appcAmount: BigDecimal, bonus: BigDecimal?, skuDescription: String,
                    skuId: String?, feeFiatAmount: BigDecimal, carrierName: String,
                    carrierImage: String): CarrierFeeFragment {
      val fragment = CarrierFeeFragment()
      fragment.arguments = Bundle().apply {
        putString(UID_KEY, uid)
        putString(DOMAIN_KEY, domain)
        putString(TRANSACTION_DATA_KEY, transactionData)
        putString(TRANSACTION_TYPE_KEY, transactionType)
        putString(PAYMENT_URL_KEY, paymentUrl)
        putString(CURRENCY_KEY, currency)
        putSerializable(FIAT_AMOUNT_KEY, amount)
        putSerializable(APPC_AMOUNT_KEY, appcAmount)
        putSerializable(BONUS_AMOUNT_KEY, bonus)
        putString(SKU_DESCRIPTION, skuDescription)
        putString(SKU_ID, skuId)
        putSerializable(FEE_FIAT_AMOUNT, feeFiatAmount)
        putString(CARRIER_NAME, carrierName)
        putString(CARRIER_IMAGE, carrierImage)
      }
      return fragment
    }
  }
}