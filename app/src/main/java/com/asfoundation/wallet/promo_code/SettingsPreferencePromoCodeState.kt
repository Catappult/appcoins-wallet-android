package com.asfoundation.wallet.promo_code

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.asf.wallet.R
import com.asfoundation.wallet.promo_code.repository.PromoCode

class SettingsPreferencePromoCodeState(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {
  private var promoCode: PromoCode? = null
  private var promoCodeState: TextView? = null
  var preferenceClickListener: View.OnClickListener? = null

  init {
    this.layoutResource = R.layout.preference_promo_code_layout
  }

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    promoCodeState = holder.findViewById(R.id.settings_promo_code_state) as TextView
    setCurrencyTextView()
  }

  fun setPromoCode(promoCode: PromoCode) {
    this.promoCode = promoCode
    setCurrencyTextView()
  }

  private fun setCurrencyTextView() {
    when {
      promoCode?.code != null -> {
        if (promoCode?.expired == false){
          promoCodeState?.text = context.getString(R.string.promo_code_active_tag)
          promoCodeState?.setTextColor(
            ResourcesCompat.getColor(context.resources, R.color.gamification_green, null))
        } else {
          promoCodeState?.text = context.getString(R.string.promo_code_expired_tag)
          promoCodeState?.setTextColor(ResourcesCompat.getColor(context.resources, R.color.red, null))
        }
      }
      else -> {
        promoCodeState?.text = null
        promoCodeState?.setTextColor(
            ResourcesCompat.getColor(context.resources, R.color.black, null))
      }
    }
  }
}