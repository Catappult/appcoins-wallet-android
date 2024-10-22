package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Typeface
import android.view.ContextThemeWrapper
import android.view.MenuItem
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
import com.asf.wallet.databinding.ItemTopupPaymentMethodBinding
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.manage_cards.models.StoredCard
import io.reactivex.disposables.CompositeDisposable

class TopupPaymentMethodsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  private val binding by lazy { ItemTopupPaymentMethodBinding.bind(itemView) }

  fun bind(
    data: PaymentMethod,
    checked: Boolean,
    listener: View.OnClickListener,
    onClickLogoutAction: () -> Unit,
    disposables: CompositeDisposable,
    showLogoutAction: Boolean,
    cardData: StoredCard?,
    onChangeCardCallback: () -> Unit,
  ) {
    val imageUrl = if (data.id == "credit_card" && cardData != null)
      cardData.cardIcon
    else
      data.iconUrl

    GlideApp.with(itemView.context)
      .load(imageUrl)
      .into(binding.paymentMethodIc)

    val selected = data.isEnabled && checked
    binding.radioButton.isChecked = selected
    binding.radioButton.isEnabled = data.isEnabled

    handleDescription(data, selected, data.isEnabled, cardData, onChangeCardCallback)
    handleFee(data.fee, data.price)
    handleMessage(data.message)

    binding.selectedBackground.visibility = View.VISIBLE

    if (data.isEnabled) {
      itemView.setOnClickListener(listener)
      binding.radioButton.visibility = View.VISIBLE
      hideDisableReason()
      removeAlphaScale(binding.paymentMethodIc)
    } else {
      itemView.setOnClickListener(null)
      binding.radioButton.visibility = View.INVISIBLE
      itemView.background = null
      if (data.disabledReason != null) {
        showDisableReason(data.disabledReason)
      } else {
        hideDisableReason()
      }

      applyAlphaScale(binding.paymentMethodIc)
    }
    if (data.showLogout) {
      binding.paymentMoreLogout.isVisible = showLogoutAction

      binding.paymentMoreLogout.setOnClickListener {
        val wrapper: Context =
          ContextThemeWrapper(itemView.context.applicationContext, R.style.CustomLogoutPopUpStyle)
        val popup = PopupMenu(wrapper, it)
        popup.menuInflater.inflate(R.menu.logout_menu, popup.menu)
        popup.setOnMenuItemClickListener { _: MenuItem ->
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

  private fun handleDescription(
    data: PaymentMethod,
    selected: Boolean,
    isEnabled: Boolean,
    cardData: StoredCard?,
    onChangeCardCallback: () -> Unit,
  ) {
    if (cardData != null && data.id == "credit_card") {
      binding.paymentMethodDescription.text = "**** ".plus(cardData.cardLastNumbers)
      binding.changeCardButton.visibility = if (selected) View.VISIBLE else View.GONE
      binding.changeCardButton.setOnClickListener {
        onChangeCardCallback()
      }
    } else {
      binding.paymentMethodDescription.text = data.label
    }
    if (selected) {
      binding.paymentMethodDescription.setTextColor(
        ContextCompat.getColor(itemView.context, R.color.styleguide_light_grey)
      )
      binding.paymentMethodDescription.typeface =
        Typeface.create("sans-serif-medium", Typeface.BOLD)
    } else {
      binding.paymentMethodDescription.setTextColor(
        ContextCompat.getColor(itemView.context, R.color.styleguide_light_grey)
      )
      binding.paymentMethodDescription.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    if (!isEnabled) {
      binding.paymentMethodDescription.setTextColor(
        ContextCompat.getColor(itemView.context, R.color.styleguide_dark_grey)
      )
      binding.paymentMethodDescription.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
  }

  private fun handleFee(fee: PaymentMethodFee?, price: FiatValue) {
    if (fee?.isValidFee() == true) {
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

  private fun handleMessage(message: String?) {
    if (message.isNullOrEmpty()) {
      binding.paymentMethodMessage.visibility = View.GONE
    } else {
      binding.paymentMethodMessage.visibility = View.VISIBLE
      binding.paymentMethodMessage.text = message
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
      binding.paymentMethodReason.text = itemView.context.getString(it)
    }
  }

  private fun hideDisableReason() {
    binding.paymentMethodReason.visibility = View.GONE
    binding.paymentMethodReason.text = null
  }


}