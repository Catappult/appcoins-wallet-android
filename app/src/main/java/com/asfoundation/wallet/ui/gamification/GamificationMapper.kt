package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.asf.wallet.R

class GamificationMapper(private val context: Context) {

  fun mapCurrentLevelInfo(level: Int): CurrentLevelInfo {
    return when (level) {
      0 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_earth),
          getColor(R.color.gamification_light_green),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      1 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_moon),
          getColor(R.color.gamification_light_grey),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      2 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_mars),
          getColor(R.color.gamification_red),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      3 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_phobos),
          getColor(R.color.gamification_blue_grey),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      4 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_jupiter),
          getColor(R.color.gamification_orange),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      5 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_europa),
          getColor(R.color.gamification_dark_yellow),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      6 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_saturn),
          getColor(R.color.gamification_yellow),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      7 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_titan),
          getColor(R.color.gamification_blue_green),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      8 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_uranus),
          getColor(R.color.gamification_old_blue),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      9 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_neptune),
          getColor(R.color.gamification_blue),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      10 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_purple),
          getColor(R.color.gamification_purple),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      11 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_green),
          getColor(R.color.gamification_green),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      12 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_brown),
          getColor(R.color.gamification_brown),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      13 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_blue),
          getColor(R.color.gamification_light_blue),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      14 -> CurrentLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_red),
          getColor(R.color.gamification_dark_red),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      else -> CurrentLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_purple),
          getColor(R.color.gamification_purple),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
    }
  }

  fun mapReachedLevelInfo(level: Int): ReachedLevelInfo {
    return when (level) {
      0 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_earth_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      1 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_moon_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      2 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_mars_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      3 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_phobos_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      4 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_jupiter_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      5 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_europa_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      6 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_saturn_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      7 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_titan_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      8 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_uranus_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      9 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_neptune_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      10 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_purple_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      11 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_green_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      12 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_brown_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      13 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_blue_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      14 -> ReachedLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_red_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
      else -> ReachedLevelInfo(getDrawable(R.drawable.gamification_unknown_planet_purple_reached),
          getString(R.string.gamification_a_comet_title),
          getString(R.string.gamification_a_comet_subtitle))
    }
  }

  private fun getDrawable(@DrawableRes drawable: Int): Drawable? {
    return ResourcesCompat.getDrawable(context.resources, drawable, null)
  }


  private fun getColor(@ColorRes color: Int): Int {
    return ResourcesCompat.getColor(context.resources, color, null)
  }

  private fun getString(@StringRes string: Int) = context.getString(string)

}