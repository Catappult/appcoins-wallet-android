package com.asfoundation.wallet.ui.gamification

import android.view.View
import kotlinx.android.synthetic.main.reached_level_layout.view.*

class LevelReachedViewHolder(itemView: View, private val mapper: GamificationMapper) :
    LevelsViewHolder(itemView) {

  override fun bind(level: LevelItem) {
    val reachedLevelInfo = mapper.mapReachedLevelInfo(level.level)
    itemView.level_icon.setImageDrawable(reachedLevelInfo.planet)
    itemView.level_title.text = reachedLevelInfo.reachedTitle
    itemView.level_description.text = reachedLevelInfo.reachedSubtitle
  }
}
