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
import androidx.recyclerview.widget.RecyclerView
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asf.wallet.databinding.ItemPaymentMethodBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.Subject

class PaymentMethodsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  private val binding by lazy { ItemPaymentMethodBinding.bind(itemView) }

  fun bind(
    data: PaymentMethod,
    checked: Boolean,
    listener: View.OnClickListener,
    onClickListenerTopup: View.OnClickListener,
    onClickPaypalLogout: () -> Unit,
    disposables: CompositeDisposable,
    showPayPalLogout: Subject<Boolean>
  ) {
    GlideApp.with(itemView.context)
      .load(data.iconUrl)
      .into(binding.paymentMethodIc)

    val selected = data.isEnabled && checked
    binding.radioButton.isChecked = selected
    binding.radioButton.isEnabled = data.isEnabled

    handleDescription(data, selected, data.isEnabled)
    handleFee(data.fee, data.isEnabled)

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
      if (data.disabledReason != null) {
        showDisableReason(data.disabledReason)
      } else {
        hideDisableReason()
      }

      applyAlphaScale(binding.paymentMethodIc)
    }
    binding.checkoutTopupButton.setOnClickListener(onClickListenerTopup)
    if (data.showTopup) {
      binding.checkoutTopupButton.visibility = View.VISIBLE
      binding.radioButton.visibility = View.GONE
    } else {
      binding.checkoutTopupButton.visibility = View.GONE
      binding.radioButton.visibility = View.VISIBLE
    }
    if (data.showLogout) {
      disposables.add(
        showPayPalLogout
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          binding.paymentMoreLogout.visibility = if (it!!)
            View.VISIBLE
          else
            View.GONE
        }
      )

      binding.paymentMoreLogout.setOnClickListener {
        var wrapper: Context =  ContextThemeWrapper(itemView.context.applicationContext, R.style.CustomLogoutPopUpStyle)
        val popup = PopupMenu(wrapper, it)
        popup.menuInflater.inflate(R.menu.logout_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
          binding.paymentMoreLogout.visibility = View.GONE
          onClickPaypalLogout()
          return@setOnMenuItemClickListener true
        }
        popup.show()
      }
    } else {
      binding.paymentMoreLogout.visibility = View.GONE
    }
    binding.paymentMethodInfo.visibility =
      if (data.showExtraFeesMessage) View.VISIBLE else View.GONE
    binding.paymentMethodInfoImage.visibility =
      if (data.showExtraFeesMessage) View.VISIBLE else View.GONE

  }

  private fun handleDescription(data: PaymentMethod, selected: Boolean, isEnabled: Boolean) {
    binding.paymentMethodDescription.text = data.label
    if (selected) {
      binding.paymentMethodDescription.setTextColor(
        ContextCompat.getColor(itemView.context, R.color.styleguide_white)
      )
      binding.paymentMethodDescription.typeface =
        Typeface.create("sans-serif-medium", Typeface.BOLD)
    } else {
      binding.paymentMethodDescription.setTextColor(  //
        ContextCompat.getColor(itemView.context, R.color.styleguide_black_transparent_80)
      )
      binding.paymentMethodDescription.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    if(!isEnabled) {
      binding.paymentMethodDescription.setTextColor(
        ContextCompat.getColor(itemView.context, R.color.styleguide_light_grey)
      )
      binding.paymentMethodDescription.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
  }

  private fun handleFee(fee: PaymentMethodFee?, enabled: Boolean) {
    if (fee?.isValidFee() == true) {
      binding.paymentMethodFee.visibility = View.VISIBLE
      val formattedValue = CurrencyFormatUtils()
        .formatPaymentCurrency(fee.amount!!, WalletCurrency.FIAT)
      binding.paymentMethodFeeValue.text = "$formattedValue ${fee.currency}"

      binding.paymentMethodFeeValue.apply {
        if (enabled) {
          this.setTextColor(ContextCompat.getColor(itemView.context, R.color.styleguide_pink))
          this.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        } else {
          this.setTextColor(
            ContextCompat.getColor(
              itemView.context,
              R.color.styleguide_black_transparent_80
            )
          )
          this.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
      }

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
      binding.paymentMethodReason.text = itemView.context.getString(it)
    }
  }

  private fun hideDisableReason() {
    binding.paymentMethodReason.visibility = View.GONE
    binding.paymentMethodReason.text = null
  }


}