package com.asfoundation.wallet.ui.gamification

import android.view.View
import com.asf.wallet.databinding.ReachedLevelLayoutBinding

class LevelReachedViewHolder(itemView: View, private val mapper: GamificationMapper) :
    LevelsViewHolder(itemView) {

  private val binding by lazy { ReachedLevelLayoutBinding.bind(itemView) }

  override fun bind(level: LevelItem) {
    val reachedLevelInfo = mapper.mapReachedLevelInfo(level.level)
    binding.levelIcon.setImageDrawable(reachedLevelInfo.planet)
    binding.levelTitle.text = reachedLevelInfo.reachedTitle
    binding.levelDescription.text = reachedLevelInfo.reachedSubtitle
  }
}
