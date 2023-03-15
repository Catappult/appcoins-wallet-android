package com.asfoundation.wallet.ui.iab.payments.common

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.common.WalletCurrency
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

  private var showHeader: Boolean = true

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    inflate(context, R.layout.layout_purchase_bonus, this)
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
    showHeader = false
    bonus_layout.visibility = View.INVISIBLE
  }

  fun showPurchaseBonusHeader() {
    showHeader = true
    bonus_layout.visibility = View.VISIBLE
  }

  fun setPurchaseBonusDescription(description: Int) {
    bonus_msg.text = context.getString(description)
    bonus_msg.visibility = View.VISIBLE
    bonus_layout.visibility = View.INVISIBLE
  }

  fun showSkeleton() {
    if (showHeader) {
      bonus_layout.visibility = View.INVISIBLE
      bonus_layout_skeleton.visibility = View.VISIBLE
    }
    bonus_msg_skeleton.visibility = View.VISIBLE
  }

  fun hideSkeleton() {
    if (showHeader) {
      bonus_layout.visibility = View.VISIBLE
      bonus_layout_skeleton.visibility = View.GONE
    }
    bonus_msg_skeleton.visibility = View.GONE
  }
}