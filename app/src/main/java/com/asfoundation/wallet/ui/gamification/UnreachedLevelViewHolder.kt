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

  override fun bind(level: LevelItem) {
      binding.lockedText.text = itemView.context.getString(R.string.gamif_next_goals,
        currencyFormatUtils.formatGamificationValues(level.amount))
    val df = DecimalFormat("###.#")
        binding.lockedBonus.text =
        itemView.context.getString(R.string.gamif_bonus, df.format(level.bonus))
  }
}