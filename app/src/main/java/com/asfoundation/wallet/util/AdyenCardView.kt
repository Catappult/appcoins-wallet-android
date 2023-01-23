package com.asfoundation.wallet.util

import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardView
import com.adyen.checkout.components.ui.view.RoundCornerImageView
import com.asf.wallet.R
import com.google.android.material.textfield.TextInputLayout

class AdyenCardView(view: View?) {

  private val cardView: CardView? = view?.findViewById(R.id.adyen_card_form_pre_selected)
  private val adyenCardNumberLayout: TextInputLayout? =
    cardView?.findViewById(R.id.textInputLayout_cardNumber)
  private val adyenExpiryDateLayout: TextInputLayout? =
    cardView?.findViewById(R.id.textInputLayout_expiryDate)
  private val adyenSecurityCodeLayout: TextInputLayout? =
    cardView?.findViewById(R.id.textInputLayout_securityCode)
  private val adyenCardImageLayout: RoundCornerImageView? =
    cardView?.findViewById(R.id.cardBrandLogo_imageView_primary)
  private val adyenSaveDetailsSwitch: SwitchCompat? =
    (cardView?.findViewById(R.id.switch_storePaymentMethod) as SwitchCompat?)?.apply {
      thumbTintList = AppCompatResources.getColorStateList(context, R.color.styleguide_pink)
      trackTintList = AppCompatResources.getColorStateList(context, R.color.styleguide_pink_transparent_40)
      isChecked = true
    }

  val cardNumber
    get() = adyenCardNumberLayout?.editText?.text

  val cardImage
    get() = adyenCardImageLayout?.drawable

  val cardSave
    get() = adyenSaveDetailsSwitch?.isChecked ?: true

  fun setError(value: String?) {
    adyenSecurityCodeLayout?.error = value
  }

  fun showInputFields(show: Boolean) {
    adyenCardNumberLayout?.visibility = if (show) View.VISIBLE else View.GONE
    adyenCardImageLayout?.visibility = if (show) View.VISIBLE else View.GONE
  }

  fun <T> clear(owner: T) where T : SavedStateRegistryOwner, T : ViewModelStoreOwner {
    adyenCardNumberLayout?.editText?.text = null
    adyenCardNumberLayout?.editText?.isEnabled = true
    adyenExpiryDateLayout?.editText?.text = null
    adyenExpiryDateLayout?.editText?.isEnabled = true
    adyenSecurityCodeLayout?.editText?.text = null
    adyenCardNumberLayout?.requestFocus()
    adyenSecurityCodeLayout?.error = null
    adyenSaveDetailsSwitch?.isChecked = true
    owner.viewModelStore.clear()
    owner.unregisterProvider(CardComponent::class.java.canonicalName)
  }
}

fun SavedStateRegistryOwner.unregisterProvider(className: String?) =
  savedStateRegistry.unregisterSavedStateProvider(
    "androidx.lifecycle.ViewModelProvider.DefaultKey:$className"
  )
