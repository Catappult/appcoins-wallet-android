package com.asfoundation.wallet.ui.iab.payments.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.LayoutPurchaseBonusBinding
import java.math.BigDecimal

/**
 * Custom view for purchase bonus
 *
 * *Future note:* no_bonus_msg view was removed. Hiding the header and setting bonus description
 * achieves the same effect.
 */
class PurchaseBonusView : FrameLayout {

  private val binding =
    LayoutPurchaseBonusBinding.inflate(LayoutInflater.from(context), this, false)
  private val formatter = CurrencyFormatUtils()

  private var showHeader: Boolean = true

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context, attrs,
    defStyleAttr
  )

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
      context.getString(R.string.gamification_purchase_header_part_2, bonusMessageValue)
    )
  }

  fun setPurchaseBonusHeaderValue(valueText: String) {
    binding.bonusValue.text = valueText
  }

  fun hidePurchaseBonusHeader() {
    showHeader = false
    binding.bonusLayout.visibility = View.INVISIBLE
  }

  fun showPurchaseBonusHeader() {
    showHeader = true
    binding.bonusLayout.visibility = View.VISIBLE
  }

  fun setPurchaseBonusDescription(description: Int) {
    binding.bonusMsg.text = context.getString(description)
    binding.bonusMsg.visibility = View.VISIBLE
    binding.bonusLayout.visibility = View.INVISIBLE
  }

  fun showSkeleton() {
    if (showHeader) {
      binding.bonusLayout.visibility = View.INVISIBLE
      binding.bonusLayoutSkeleton.root.visibility = View.VISIBLE
    }
    binding.bonusMsgSkeleton.root.visibility = View.VISIBLE
  }

  fun hideSkeleton() {
    if (showHeader) {
      binding.bonusLayout.visibility = View.VISIBLE
      binding.bonusLayoutSkeleton.root.visibility = View.GONE
    }
    binding.bonusMsgSkeleton.root.visibility = View.GONE
  }
}