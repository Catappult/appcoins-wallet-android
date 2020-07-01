package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.appcoins.wallet.gamification.LevelViewModel
import com.asf.wallet.R
import kotlinx.android.synthetic.main.reached_level_layout.view.*

class LevelReachedViewHolder(itemView: View, private val context: Context) :
    LevelsViewHolder(itemView) {

  override fun bind(level: LevelViewModel) {
    itemView.level_title.text = "Level: " + level.level
    itemView.level_description.text = "You started here"
    handleImage(level.level)
  }

  private fun handleImage(level: Int) {
    when (level) {
      0 -> setImage(R.drawable.gamification_earth_reached)
      1 -> setImage(R.drawable.gamification_moon_reached)
      2 -> setImage(R.drawable.gamification_mars_reached)
      3 -> setImage(R.drawable.gamification_phobos_reached)
      4 -> setImage(R.drawable.gamification_jupiter_reached)
      5 -> setImage(R.drawable.gamification_europa_reached)
      6 -> setImage(R.drawable.gamification_saturn_reached)
      7 -> setImage(R.drawable.gamification_titan_reached)
      8 -> setImage(R.drawable.gamification_uranus_reached)
      9 -> setImage(R.drawable.gamification_neptune_reached)
      //TODO Change for future unknown planet
      10 -> setImage(R.drawable.gamification_planetx_reached)
      11 -> setImage(R.drawable.gamification_planetx_reached)
      12 -> setImage(R.drawable.gamification_planetx_reached)
      13 -> setImage(R.drawable.gamification_planetx_reached)
      14 -> setImage(R.drawable.gamification_planetx_reached)
      else -> setImage(R.drawable.gamification_planetx_reached)
    }
  }

  private fun setImage(@DrawableRes drawable: Int) {
    itemView.level_icon.setImageDrawable(
        ResourcesCompat.getDrawable(context.resources, drawable, null))
  }
}
