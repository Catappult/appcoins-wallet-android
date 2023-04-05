package com.asfoundation.wallet.ui.gamification

import android.view.View
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.databinding.UnreachedLevelLayoutBinding
import java.text.DecimalFormat

class UnreachedLevelViewHolder(
    itemView: View,
    private val currencyFormatUtils: CurrencyFormatUtils
) :
    LevelsViewHolder(itemView) {

    private val binding by lazy { UnreachedLevelLayoutBinding.bind(itemView) }

    private val locked_text get() = binding.lockedText
    private val locked_bonus get() = binding.lockedBonus

  override fun bind(level: LevelItem) {
    locked_text.text = itemView.context.getString(R.string.gamif_next_goals,
        currencyFormatUtils.formatGamificationValues(level.amount))
    val df = DecimalFormat("###.#")
    locked_bonus.text =
        itemView.context.getString(R.string.gamif_bonus, df.format(level.bonus))
  }
}