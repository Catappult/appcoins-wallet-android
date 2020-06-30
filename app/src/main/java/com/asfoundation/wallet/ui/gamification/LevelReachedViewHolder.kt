package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.appcoins.wallet.gamification.LevelViewModel
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import kotlinx.android.synthetic.main.reached_level_layout.view.*

class LevelReachedViewHolder(itemView: View, private val context: Context,
                             private val currencyFormatUtils: CurrencyFormatUtils) :
    LevelsViewHolder(itemView) {

  override fun bind(level: LevelViewModel) {
    itemView.level_title.text = "Level: " + level.level
    itemView.level_description.text = "You started here"
    handleImage(level.level)
  }

  private fun handleImage(level: Int) {
    when (level) {
      0 -> setImage(R.drawable.ic_level_comet_dark)
      1 -> setImage(R.drawable.ic_level_star_dark)
      2 -> setImage(R.drawable.ic_level_moon_dark)
      3 -> setImage(R.drawable.ic_level_planet_dark)
      4 -> setImage(R.drawable.ic_level_galaxy_dark)
      5 -> setImage(R.drawable.ic_level_comet)
      6 -> setImage(R.drawable.ic_level_star)
      7 -> setImage(R.drawable.ic_level_moon)
      8 -> setImage(R.drawable.ic_level_planet)
      9 -> setImage(R.drawable.ic_level_galaxy)
    }
  }

  private fun setImage(@DrawableRes drawable: Int) {
    itemView.level_icon.setImageDrawable(
        ResourcesCompat.getDrawable(context.resources, drawable, null))
  }
}
