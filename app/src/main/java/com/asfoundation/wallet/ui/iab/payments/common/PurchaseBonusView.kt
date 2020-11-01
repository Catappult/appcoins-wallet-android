package com.asfoundation.wallet.ui.iab.payments.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import kotlinx.android.synthetic.main.layout_purchase_bonus.view.*
import java.math.BigDecimal

/**
 * Custom view for purchase bonus
 *
 * *Future note:* no_bonus_msg view was removed. Hiding the header and setting bonus description
 * achieves the same effect.
 */
class PurchaseBonusView : FrameLayout {
  private val formatter = CurrencyFormatUtils()

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    inflate(context, R.layout.layout_purchase_bonus, this)
  }

  fun setPurchaseBonusHeaderTitle(title: String) {
    bonus_header_1.text = title
  }

  fun setPurchaseBonusHeaderValue(bonus: BigDecimal, currencySymbol: String) {
    var scaledBonus = bonus.stripTrailingZeros()
        .setScale(CurrencyFormatUtils.FIAT_SCALE, BigDecimal.ROUND_DOWN)
    var newCurrencyString = currencySymbol
    if (scaledBonus < BigDecimal("0.01")) {
      newCurrencyString = "~$currencySymbol"
    }
    scaledBonus = scaledBonus.max(BigDecimal("0.01"))
    val formattedBonus = formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    val bonusMessageValue = newCurrencyString + formattedBonus
    setPurchaseBonusHeaderValue(
        context.getString(R.string.gamification_purchase_header_part_2, bonusMessageValue))

  }

  fun setPurchaseBonusHeaderValue(valueText: String) {
    bonus_value.text = valueText
  }

  fun hidePurchaseBonusHeader() {
    bonus_layout.visibility = View.GONE
  }

  fun setPurchaseBonusDescription(description: String) {
    bonus_msg.visibility = View.GONE
  }

  fun showSkeleton() {
    bonus_layout_skeleton.visibility = View.VISIBLE
    bonus_msg_skeleton.visibility = View.VISIBLE
  }

  fun hideSkeleton() {
    bonus_layout_skeleton.visibility = View.GONE
    bonus_msg_skeleton.visibility = View.GONE
  }


}