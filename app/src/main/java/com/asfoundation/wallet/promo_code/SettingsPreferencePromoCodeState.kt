package com.asfoundation.wallet.promo_code

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.asf.wallet.R
import com.asfoundation.wallet.promo_code.repository.PromoCodeEntity

class SettingsPreferencePromoCodeState(context: Context?, attrs: AttributeSet?) :
    Preference(context, attrs) {
  private var promoCode: PromoCodeEntity? = null
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

  fun setPromoCode(promoCode: PromoCodeEntity) {
    Log.d("APPC-2709", "SettingsPreferencePromoCodeState : setPromoCode $promoCode")
    this.promoCode = promoCode
    setCurrencyTextView()
  }

  private fun setCurrencyTextView() {
    when {
      promoCode?.code != "" && promoCode?.expired == null -> {
        promoCodeState?.text = "Active"
        promoCodeState?.setTextColor(
            ResourcesCompat.getColor(context.resources, R.color.gamification_green, null))
      }
      promoCode?.code != "" && promoCode?.expired != null -> {
        promoCodeState?.text = "Expired"
        promoCodeState?.setTextColor(ResourcesCompat.getColor(context.resources, R.color.red, null))
      }
      else -> {
        promoCodeState?.text = "Test"
        promoCodeState?.setTextColor(
            ResourcesCompat.getColor(context.resources, R.color.black, null))
      }
    }

  }
}