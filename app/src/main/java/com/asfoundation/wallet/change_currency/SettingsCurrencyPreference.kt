package com.asfoundation.wallet.change_currency

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.appcoins.wallet.core.utils.android_common.extensions.safeLet
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class SettingsCurrencyPreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {
  private var selectedCurrency: FiatCurrencyEntity? = null
  private var currency: TextView? = null
  private var flag: ImageView? = null
  var preferenceClickListener: View.OnClickListener? = null

  init {
    this.layoutResource = R.layout.preferences_with_active_currency_layout
  }

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    currency = holder.findViewById(R.id.settings_currency_text) as TextView
    flag = holder.findViewById(R.id.settings_flag_ic) as ImageView
    setCurrencyTextView()
    setFlagImageView()
  }

  fun setCurrency(selectedCurrency: FiatCurrencyEntity) {
    this.selectedCurrency = selectedCurrency
    setCurrencyTextView()
    setFlagImageView()
  }

  private fun setCurrencyTextView() {
    currency?.text = selectedCurrency?.currency
  }

  private fun setFlagImageView() {
    safeLet(flag, selectedCurrency?.flag) { flagView, flagUrl ->
      GlideApp.with(context)
          .load(Uri.parse(flagUrl))
          .transition(DrawableTransitionOptions.withCrossFade())
          .circleCrop()
          .into(flagView)
    }
  }
}