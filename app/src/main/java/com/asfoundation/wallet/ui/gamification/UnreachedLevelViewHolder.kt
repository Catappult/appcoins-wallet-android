package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.view.View
import com.appcoins.wallet.gamification.LevelViewModel
import com.asfoundation.wallet.util.CurrencyFormatUtils
import kotlinx.android.synthetic.main.unreached_level_layout.view.*

class UnreachedLevelViewHolder(itemView: View, private val context: Context,
                               private val currencyFormatUtils: CurrencyFormatUtils) :
    LevelsViewHolder(itemView) {

  override fun bind(level: LevelViewModel) {
    itemView.locked_text.text =
        "Spend ${currencyFormatUtils.formatGamificationValues(level.amount)} to reach this level"
    itemView.locked_bonus.text = "${level.bonus}% Bonus"
  }
}