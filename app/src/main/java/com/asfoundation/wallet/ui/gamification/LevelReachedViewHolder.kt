package com.asfoundation.wallet.ui.gamification

import android.view.View
import com.asf.wallet.databinding.ReachedLevelLayoutBinding

class LevelReachedViewHolder(itemView: View, private val mapper: GamificationMapper) :
    LevelsViewHolder(itemView) {

  private val binding by lazy { ReachedLevelLayoutBinding.bind(itemView) }

  private val level_icon get() = binding.levelIcon
  private val level_title get() = binding.levelTitle
  private val level_description get() = binding.levelDescription

  override fun bind(level: LevelItem) {
    val reachedLevelInfo = mapper.mapReachedLevelInfo(level.level)
    level_icon.setImageDrawable(reachedLevelInfo.planet)
    level_title.text = reachedLevelInfo.reachedTitle
    level_description.text = reachedLevelInfo.reachedSubtitle
  }
}
