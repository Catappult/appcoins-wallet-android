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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.ui.iab.IabView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.android_common.extensions.getStringSpanned
import com.appcoins.wallet.ui.common.withNoLayoutTransition
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import com.asf.wallet.databinding.FragmentCarrierConfirmBinding
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CarrierFeeFragment : BasePageViewFragment(), CarrierFeeView {

  private val formatter = CurrencyFormatUtils()

  @Inject
  lateinit var presenter: CarrierFeePresenter

  lateinit var iabView: IabView

  private val views by viewBinding(FragmentCarrierConfirmBinding::bind)

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
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

    (views.dialogBuyButtonsPaymentMethods?.cancelButton ?: views.dialogBuyButtons?.cancelButton)?.run {
      setText(getString(R.string.back_button))
      visibility = View.VISIBLE
    }

    (views.dialogBuyButtonsPaymentMethods?.buyButton ?: views.dialogBuyButtons?.buyButton)?.run {
      setText(getString(R.string.action_next))
      visibility = View.VISIBLE
      isEnabled = false
    }

  }

  override fun initializeView(
    currency: String, fiatAmount: BigDecimal,
    appcAmount: BigDecimal, skuDescription: String,
    bonusAmount: BigDecimal?, carrierName: String,
    carrierImage: String, carrierFeeFiat: BigDecimal
  ) {
    (views.dialogBuyButtonsPaymentMethods?.buyButton ?: views.dialogBuyButtons?.buyButton!!).isEnabled = true
    views.paymentMethodsHeader.setDescription(skuDescription)
    views.paymentMethodsHeader.hidePrice(
      resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    )
    views.paymentMethodsHeader.hideSkeleton()

    val fiat =
      "${
        formatter.formatPaymentCurrency(fiatAmount + carrierFeeFiat, WalletCurrency.FIAT)
      } $currency"
    val appc = "${
      formatter.formatPaymentCurrency(
        appcAmount,
        WalletCurrency.APPCOINS
      )
    } ${WalletCurrency.APPCOINS.symbol}"
    views.fiatPriceText.text = fiat
    views.appcPriceText.text = appc

    val feeString: SpannedString = buildSpannedString {
      color(ResourcesCompat.getColor(resources, R.color.styleguide_pink, null)) {
        append("${formatter.formatPaymentCurrency(carrierFeeFiat, WalletCurrency.FIAT)} $currency")
      }
    }
    views.feeTitle.text =
      context?.getStringSpanned(R.string.carrier_billing_carrier_fees_body, feeString)

    GlideApp.with(requireContext())
      .load(carrierImage)
      .into(views.carrierImage)

    views.purchaseBonus.withNoLayoutTransition {
      if (bonusAmount != null) {
        views.purchaseBonus.visibility = View.VISIBLE
        views.purchaseBonus.setPurchaseBonusHeaderValue(bonusAmount, mapCurrencyCodeToSymbol(currency))
        views.purchaseBonus.hideSkeleton()
      } else {
        views.purchaseBonus.visibility = View.GONE
      }
    }
  }

  override fun setAppDetails(appName: String, icon: Drawable) {
    views.paymentMethodsHeader.setTitle(appName)
    views.paymentMethodsHeader.setIcon(icon)
  }

  private fun mapCurrencyCodeToSymbol(currencyCode: String): String {
    return if (currencyCode.equals("APPC", ignoreCase = true))
      currencyCode
    else
      Currency.getInstance(currencyCode)
        .symbol
  }

  override fun cancelButtonEvent(): Observable<Any> {
    return RxView.clicks((views.dialogBuyButtonsPaymentMethods?.cancelButton ?: views.dialogBuyButtons?.cancelButton!!))
  }

  override fun systemBackEvent(): Observable<Any> {
    return iabView.backButtonPress()
  }


  override fun nextClickEvent(): Observable<Any> = RxView.clicks((views.dialogBuyButtonsPaymentMethods?.buyButton ?: views.dialogBuyButtons?.buyButton!!))

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
    internal const val SKU_DESCRIPTION_KEY = "sku_description"
    internal const val SKU_ID_KEY = "sku_id"
    internal const val FEE_FIAT_AMOUNT_KEY = "fee_fiat_amount"
    internal const val CARRIER_NAME_KEY = "carrier_name"
    internal const val CARRIER_IMAGE_KEY = "carrier_image"
    internal const val PHONE_NUMBER_KEY = "phone_number"

    @JvmStatic
    fun newInstance(
      uid: String, domain: String, transactionData: String, transactionType: String,
      paymentUrl: String?, currency: String?, amount: BigDecimal,
      appcAmount: BigDecimal, bonus: BigDecimal?, skuDescription: String,
      skuId: String?, feeFiatAmount: BigDecimal, carrierName: String,
      carrierImage: String, phoneNumber: String
    ): CarrierFeeFragment {
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
        putString(SKU_DESCRIPTION_KEY, skuDescription)
        putString(SKU_ID_KEY, skuId)
        putSerializable(FEE_FIAT_AMOUNT_KEY, feeFiatAmount)
        putString(CARRIER_NAME_KEY, carrierName)
        putString(CARRIER_IMAGE_KEY, carrierImage)
        putString(PHONE_NUMBER_KEY, phoneNumber)
      }
      return fragment
    }
  }
}