package com.asfoundation.wallet.promo_code

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.asf.wallet.R
import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.appcoins.wallet.feature.promocode.data.repository.ValidityState

class SettingsPreferencePromoCodeState(context: Context, attrs: AttributeSet?) :
  Preference(context, attrs) {
  private var promoCode: com.appcoins.wallet.feature.promocode.data.repository.PromoCode? = null
  private var promoCodeState: TextView? = null

  init {
    this.layoutResource = R.layout.preference_promo_code_layout
  }

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    promoCodeState = holder.findViewById(R.id.settings_promo_code_state) as TextView
    setCurrencyTextView()
  }

  fun setPromoCode(promoCode: com.appcoins.wallet.feature.promocode.data.repository.PromoCode) {
    this.promoCode = promoCode
    setCurrencyTextView()
  }

  private fun setCurrencyTextView() {
    when {
      promoCode?.code != null -> {
        when (promoCode?.validity) {
          com.appcoins.wallet.feature.promocode.data.repository.ValidityState.ACTIVE -> {
            promoCodeState?.text = context.getString(R.string.promo_code_active_tag)
            promoCodeState?.setTextColor(
              ResourcesCompat.getColor(context.resources, R.color.gamification_green, null)
            )
          }
          com.appcoins.wallet.feature.promocode.data.repository.ValidityState.EXPIRED -> {
            promoCodeState?.text = context.getString(R.string.promo_code_expired_tag)
            promoCodeState?.setTextColor(
              ResourcesCompat.getColor(
                context.resources,
                R.color.styleguide_red,
                null
              )
            )
          }
          else -> {
            promoCodeState?.text = null
            promoCodeState?.setTextColor(
              ResourcesCompat.getColor(context.resources, R.color.styleguide_black, null)
            )
          }
        }

      }
      else -> {
        promoCodeState?.text = null
        promoCodeState?.setTextColor(
          ResourcesCompat.getColor(context.resources, R.color.styleguide_black, null)
        )
      }
    }
  }
}