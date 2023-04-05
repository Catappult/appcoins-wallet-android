package com.asfoundation.wallet.ui.iab

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.ItemPaymentMethodBinding

class PaymentMethodsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  private val binding by lazy { ItemPaymentMethodBinding.bind(itemView) }

  private val payment_method_ic get() = binding.paymentMethodIc
  private val radio_button get() = binding.radioButton
  private val checkout_topup_button get() = binding.checkoutTopupButton
  private val payment_method_description get() = binding.paymentMethodDescription
  private val payment_method_fee get() = binding.paymentMethodFee
  private val payment_method_fee_value get() = binding.paymentMethodFeeValue
  private val payment_method_reason get() = binding.paymentMethodReason

  fun bind(
      data: PaymentMethod,
      checked: Boolean,
      listener: View.OnClickListener,
      onClickListener: View.OnClickListener
  ) {
    GlideApp.with(itemView.context)
        .load(data.iconUrl)
        .into(payment_method_ic)

    val selected = data.isEnabled && checked
    radio_button.isChecked = selected
    radio_button.isEnabled = data.isEnabled

    handleDescription(data, selected)
    handleFee(data.fee, data.isEnabled)

    if (data.isEnabled) {
      itemView.setOnClickListener(listener)
      radio_button.visibility = View.VISIBLE
      hideDisableReason()
      removeAlphaScale(payment_method_ic)
    } else {
      itemView.setOnClickListener(null)
      radio_button.visibility = View.INVISIBLE
      itemView.background = null
      if (data.disabledReason != null) {
        showDisableReason(data.disabledReason)
      } else {
        hideDisableReason()
      }

      applyAlphaScale(payment_method_ic)
    }
    checkout_topup_button.setOnClickListener(onClickListener)
    if (data.showTopup) {
      checkout_topup_button.visibility = View.VISIBLE
      radio_button.visibility = View.GONE
    } else {
      checkout_topup_button.visibility = View.GONE
      radio_button.visibility = View.VISIBLE
    }
  }

  private fun handleDescription(data: PaymentMethod, selected: Boolean) {
    payment_method_description.text = data.label
    if (selected) {
      payment_method_description.setTextColor(
          ContextCompat.getColor(itemView.context, R.color.styleguide_black_transparent_80))
      payment_method_description.typeface =
          Typeface.create("sans-serif-medium", Typeface.NORMAL)
    } else {
      payment_method_description.setTextColor(
          ContextCompat.getColor(itemView.context, R.color.styleguide_dark_grey))
      payment_method_description.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
  }

  private fun handleFee(fee: PaymentMethodFee?, enabled: Boolean) {
    if (fee?.isValidFee() == true) {
      payment_method_fee.visibility = View.VISIBLE
      val formattedValue = CurrencyFormatUtils()
          .formatPaymentCurrency(fee.amount!!, WalletCurrency.FIAT)
      payment_method_fee_value.text = "$formattedValue ${fee.currency}"

      payment_method_fee_value.apply {
        if (enabled) {
          this.setTextColor(ContextCompat.getColor(itemView.context, R.color.styleguide_pink))
          this.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        } else {
          this.setTextColor(ContextCompat.getColor(itemView.context, R.color.styleguide_black_transparent_80))
          this.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
      }

    } else {
      payment_method_fee.visibility = View.GONE
    }
  }

  private fun applyAlphaScale(imageView: ImageView) {
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    val filter = ColorMatrixColorFilter(colorMatrix)
    imageView.colorFilter = filter
  }

  private fun removeAlphaScale(imageView: ImageView) {
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(1f)
    val filter = ColorMatrixColorFilter(colorMatrix)
    imageView.colorFilter = filter
  }

  private fun showDisableReason(@StringRes reason: Int?) {
    reason?.let {
      payment_method_reason.visibility = View.VISIBLE
      payment_method_reason.text = itemView.context.getString(it)
    }
  }

  private fun hideDisableReason() {
    payment_method_reason.visibility = View.GONE
    payment_method_reason.text = null
  }


}