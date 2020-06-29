package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.view.View
import com.appcoins.wallet.gamification.LevelViewModel
import kotlinx.android.synthetic.main.reached_level_layout.view.*

class LevelReachedViewHolder(itemView: View, private val context: Context) :
    LevelsViewHolder(itemView) {

  override fun bind(level: LevelViewModel) {
    itemView.level_title.text = "Level: " + level.level
  }
}
