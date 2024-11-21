package com.asfoundation.wallet.ui.iab

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.asf.wallet.R
import com.asf.wallet.databinding.ItemPaymentMethodBinding
import com.asfoundation.wallet.GlideApp

class PaymentMethodsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  private val binding by lazy { ItemPaymentMethodBinding.bind(itemView) }

  fun bind(
    data: PaymentMethod,
    checked: Boolean,
    listener: View.OnClickListener,
    onClickLogoutAction: () -> Unit,
    showLogoutAction: Boolean
  ) {
    GlideApp.with(itemView.context)
      .load(data.iconUrl)
      .into(binding.paymentMethodIc)

    val selected = data.isEnabled && checked
    binding.radioButton.isChecked = selected
    binding.radioButton.isEnabled = data.isEnabled

    handleDescription(data, selected, data.isEnabled)
    handleFee(data.fee, data.price, data.isEnabled)

    binding.selectedBackground.visibility = if (selected) View.VISIBLE else View.INVISIBLE

    if (data.isEnabled) {
      itemView.setOnClickListener(listener)
      binding.radioButton.visibility = View.VISIBLE
      hideDisableReason()
      removeAlphaScale(binding.paymentMethodIc)
    } else {
      itemView.setOnClickListener(null)
      binding.radioButton.visibility = View.INVISIBLE
      itemView.background = null

      when {
        data.disabledReason != null -> {
          showDisableReason(data.disabledReason)
        }

        data.message != null -> {
          showDisableReason(data.message)
        }

        else -> {
          hideDisableReason()
        }
      }

      applyAlphaScale(binding.paymentMethodIc)
    }
    if (data.showLogout) {
      binding.paymentMoreLogout.isVisible = showLogoutAction

      binding.paymentMoreLogout.setOnClickListener {
        val popup = PopupMenu(itemView.context.applicationContext, it)
        popup.menuInflater.inflate(R.menu.logout_menu, popup.menu)
        popup.setOnMenuItemClickListener {
          binding.paymentMoreLogout.visibility = View.GONE
          onClickLogoutAction()
          return@setOnMenuItemClickListener true
        }
        popup.show()
      }
    } else {
      binding.paymentMoreLogout.visibility = View.GONE
    }
  }

  private fun handleDescription(data: PaymentMethod, selected: Boolean, isEnabled: Boolean) {
    binding.paymentMethodDescription.text = data.label
    if (selected) {
      binding.paymentMethodDescription.setTextColor(
        ContextCompat.getColor(itemView.context, R.color.styleguide_payments_main_text)
      )
    } else {
      binding.paymentMethodDescription.setTextColor(
        ContextCompat.getColor(itemView.context, R.color.styleguide_payments_main_text)
      )
      binding.paymentMethodDescription.typeface = Typeface.create("roboto_medium", Typeface.NORMAL)
    }
    if (!isEnabled) {
      binding.paymentMethodDescription.setTextColor(
        ContextCompat.getColor(itemView.context, R.color.styleguide_dark_grey)
      )
      binding.paymentMethodDescription.typeface = Typeface.create("roboto_medium", Typeface.NORMAL)
    }
  }

  private fun handleFee(fee: PaymentMethodFee?, price: FiatValue, isEnabled: Boolean) {
    if (fee?.isValidFee() == true && isEnabled) {
      binding.paymentMethodFee.visibility = View.VISIBLE
      val formattedValue = CurrencyFormatUtils()
        .formatPaymentCurrency(fee.amount!! + price.amount, WalletCurrency.FIAT)
      binding.paymentMethodFee.text =
        itemView.context.getString(
          R.string.purchase_fees_and_taxes_known_disclaimer_body,
          formattedValue,
          fee.currency
        )
    } else {
      binding.paymentMethodFee.visibility = View.GONE
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
      binding.paymentMethodReason.visibility = View.VISIBLE
      binding.paymentMethodFee.visibility = View.GONE
      binding.paymentMethodReason.text = itemView.context.getString(it)
    }
  }

  private fun showDisableReason(message: String?) {
    message?.let {
      binding.paymentMethodReason.visibility = View.VISIBLE
      binding.paymentMethodFee.visibility = View.GONE
      binding.paymentMethodReason.text = it
    }
  }

  private fun hideDisableReason() {
    binding.paymentMethodReason.visibility = View.GONE
    binding.paymentMethodReason.text = null
  }


}