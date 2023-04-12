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

  fun setTitle(title: String) {
    binding.appName.text = title
  }

  fun setDescription(description: String) {
    binding.appSkuDescription.text = description
  }

  fun setPrice(fiatAmount: BigDecimal, appcAmount: BigDecimal, currency: String) {
    val fiat = "${formatter.formatPaymentCurrency(fiatAmount, WalletCurrency.FIAT)} $currency"
    val appc = "${formatter.formatPaymentCurrency(appcAmount,
        WalletCurrency.APPCOINS)} ${WalletCurrency.APPCOINS.symbol}"
    binding.fiatPrice.text = fiat
    binding.appcPrice.text = appc
  }

  fun showPrice() {
    binding.fiatPrice.visibility = View.VISIBLE
    binding.appcPrice.visibility = View.VISIBLE
  }

  fun hidePrice(remove: Boolean) {
    if (remove) {
      binding.fiatPrice.visibility = View.GONE
      binding.appcPrice.visibility = View.GONE
    } else {
      binding.fiatPrice.visibility = View.INVISIBLE
      binding.appcPrice.visibility = View.INVISIBLE
    }
  }

  fun setIcon(icon: Drawable) {
    binding.appIcon.setImageDrawable(icon)
  }

  fun showSkeleton() {
    binding.fiatPriceSkeleton.root.visibility = View.VISIBLE
    binding.appcPriceSkeleton.root.visibility = View.VISIBLE
  }

  fun hideSkeleton() {
    binding.fiatPriceSkeleton.root.visibility = View.GONE
    binding.appcPriceSkeleton.root.visibility = View.GONE
  }
}