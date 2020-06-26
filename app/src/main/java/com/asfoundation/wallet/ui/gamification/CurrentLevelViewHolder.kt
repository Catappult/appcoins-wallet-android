package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.view.View
import com.appcoins.wallet.gamification.repository.Levels
import java.math.BigDecimal

class CurrentLevelViewHolder(itemView: View,
                             private val context: Context,
                             private val amountSpent: BigDecimal) :
    LevelsViewHolder(itemView) {

  override fun bind(level: Levels.Level) {

  }


}