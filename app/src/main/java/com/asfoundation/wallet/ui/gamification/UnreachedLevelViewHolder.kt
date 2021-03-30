package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import kotlinx.android.synthetic.main.unreached_level_layout.view.*
import java.text.DecimalFormat

class UnreachedLevelViewHolder(itemView: View, private val context: Context,
                               private val currencyFormatUtils: CurrencyFormatUtils) :
    LevelsViewHolder(itemView) {

  override fun bind(level: LevelItem) {
    itemView.locked_text.text = context.getString(R.string.gamif_next_goals,
        currencyFormatUtils.formatGamificationValues(level.amount))
    val df = DecimalFormat("###.#")
    itemView.locked_bonus.text = context.getString(R.string.gamif_bonus, df.format(level.bonus))
  }
}