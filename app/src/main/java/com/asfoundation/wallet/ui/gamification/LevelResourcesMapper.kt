package com.asfoundation.wallet.ui.gamification

import com.asf.wallet.R

class LevelResourcesMapper {
  fun mapDarkIcons(level: ViewLevel): Int {
    if (level.isCompleted) {
      return when (level.level) {
        0 -> R.drawable.ic_level_comet_dark
        1 -> R.drawable.ic_level_moon_dark
        2 -> R.drawable.ic_level_planet_dark
        3 -> R.drawable.ic_level_star_dark
        4 -> R.drawable.ic_level_galaxy_dark
        else -> {
          R.drawable.ic_level_locked
        }
      }
    }
    return R.drawable.ic_level_locked
  }

  fun mapIcons(level: Int): Int {
      return when (level) {
        0 -> R.drawable.ic_level_comet
        1 -> R.drawable.ic_level_moon
        2 -> R.drawable.ic_level_planet
        3 -> R.drawable.ic_level_star
        4 -> R.drawable.ic_level_galaxy
        else -> {
          R.drawable.ic_locked
        }
      }
  }

  fun mapImage(level: Int): Int {
    return when (level) {
      0 -> R.drawable.level_comet
      1 -> R.drawable.level_moon
      2 -> R.drawable.level_planet
      3 -> R.drawable.level_star
      4 -> R.drawable.level_galaxy
      else -> {
        R.drawable.ic_locked
      }
    }
  }

  fun mapTitle(level: Int): Int {
    return when (level) {
      0 -> R.string.gamification_a_comet_title
      1 -> R.string.gamification_a_moon_title
      2 -> R.string.gamification_a_planet_title
      3 -> R.string.gamification_a_star_title
      4 -> R.string.gamification_a_galaxy_title
      else -> {
        R.string.gamification_a_comet_title
      }
    }
  }

  fun mapSubtitle(level: Int): Int {
    return when (level) {
      0 -> R.string.gamification_a_comet_subtitle
      1 -> R.string.gamification_a_moon_subtitle
      2 -> R.string.gamification_planet_subtitle
      3 -> R.string.gamification_star_subtitle
      4 -> R.string.gamification_galaxy_subtitle
      else -> {
        R.string.gamification_a_comet_subtitle
      }
    }
  }

}
