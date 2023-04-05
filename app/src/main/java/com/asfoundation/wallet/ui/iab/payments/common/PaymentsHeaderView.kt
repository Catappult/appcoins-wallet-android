package com.asfoundation.wallet.ui.iab.payments.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.PaymentMethodsHeaderBinding
import java.math.BigDecimal

class PaymentsHeaderView : FrameLayout {
  private val formatter = CurrencyFormatUtils()

  private val binding : PaymentMethodsHeaderBinding

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    binding = PaymentMethodsHeaderBinding.inflate(LayoutInflater.from(context), this, false)
  }

  private val app_name get() = binding.appName
  private val app_sku_description get() = binding.appSkuDescription
  private val fiat_price get() = binding.fiatPrice
  private val appc_price get() = binding.appcPrice
  private val app_icon get() = binding.appIcon
  private val fiat_price_skeleton get() = binding.fiatPriceSkeleton.root
  private val appc_price_skeleton get() = binding.appcPriceSkeleton.root

  fun setTitle(title: String) {
    app_name.text = title
  }

  fun setDescription(description: String) {
    app_sku_description.text = description
  }

  fun setPrice(fiatAmount: BigDecimal, appcAmount: BigDecimal, currency: String) {
    val fiat = "${formatter.formatPaymentCurrency(fiatAmount, WalletCurrency.FIAT)} $currency"
    val appc = "${formatter.formatPaymentCurrency(appcAmount,
        WalletCurrency.APPCOINS)} ${WalletCurrency.APPCOINS.symbol}"
    fiat_price.text = fiat
    appc_price.text = appc
  }

  fun showPrice() {
    fiat_price.visibility = View.VISIBLE
    appc_price.visibility = View.VISIBLE
  }

  fun hidePrice(remove: Boolean) {
    if (remove) {
      fiat_price.visibility = View.GONE
      appc_price.visibility = View.GONE
    } else {
      fiat_price.visibility = View.INVISIBLE
      appc_price.visibility = View.INVISIBLE
    }
  }

  fun setIcon(icon: Drawable) {
    app_icon.setImageDrawable(icon)
  }

  fun showSkeleton() {
    fiat_price_skeleton.visibility = View.VISIBLE
    appc_price_skeleton.visibility = View.VISIBLE
  }

  fun hideSkeleton() {
    fiat_price_skeleton.visibility = View.GONE
    appc_price_skeleton.visibility = View.GONE
  }
}