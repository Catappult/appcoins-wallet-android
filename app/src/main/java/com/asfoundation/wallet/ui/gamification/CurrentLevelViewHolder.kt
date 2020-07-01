package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.appcoins.wallet.gamification.LevelViewModel
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.current_level_card.view.*
import kotlinx.android.synthetic.main.current_level_layout.view.*
import java.math.BigDecimal
import java.math.RoundingMode


class CurrentLevelViewHolder(itemView: View,
                             private val context: Context,
                             private val amountSpent: BigDecimal,
                             private val nextLevelAmount: BigDecimal,
                             private val currencyFormatUtils: CurrencyFormatUtils,
                             private val uiEventListener: PublishSubject<Boolean>) :
    LevelsViewHolder(itemView) {

  override fun bind(level: LevelViewModel) {
    val progress = getProgressPercentage(level.amount)
    handleSpecificLevel(level.level, currencyFormatUtils.formatGamificationValues(progress),
        level.bonus)
    setProgress(progress)
    handleToogleButton(level.level)
  }

  private fun handleSpecificLevel(level: Int, progressPercentage: String, bonus: Double) {
    when (level) {
      0 -> {
        setImage(R.drawable.gamification_earth)
        setColor(R.color.gamification_light_green)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      1 -> {
        setImage(R.drawable.gamification_moon)
        setColor(R.color.gamification_light_grey)
        setText(R.string.gamification_a_galaxy_title, R.string.gamification_galaxy_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      2 -> {
        setImage(R.drawable.gamification_mars)
        setColor(R.color.gamification_red)
        setText(R.string.gamification_a_moon_title, R.string.gamification_a_moon_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      3 -> {
        setImage(R.drawable.gamification_phobos)
        setColor(R.color.gamification_blue_grey)
        setText(R.string.gamification_a_planet_title, R.string.gamification_planet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      4 -> {
        setImage(R.drawable.gamification_jupiter)
        setColor(R.color.gamification_orange)
        setText(R.string.gamification_a_star_title, R.string.gamification_star_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      5 -> {
        setImage(R.drawable.gamification_europa)
        setColor(R.color.gamification_dark_yellow)
        setText(R.string.gamification_a_star_title, R.string.gamification_star_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      6 -> {
        setImage(R.drawable.gamification_saturn)
        setColor(R.color.gamification_yellow)
        setText(R.string.gamification_a_planet_title, R.string.gamification_planet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      7 -> {
        setImage(R.drawable.gamification_titan)
        setColor(R.color.gamification_blue_green)
        setText(R.string.gamification_a_moon_title, R.string.gamification_a_moon_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      8 -> {
        setImage(R.drawable.gamification_uranus)
        setColor(R.color.gamification_old_blue)
        setText(R.string.gamification_a_galaxy_title, R.string.gamification_galaxy_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      9 -> {
        setImage(R.drawable.gamification_neptune)
        setColor(R.color.gamification_blue)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      //TODO Change for future unknown planet
      10 -> {
        setImage(R.drawable.gamification_unknown_planet_purple)
        setColor(R.color.gamification_purple)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      11 -> {
        setImage(R.drawable.gamification_unknown_planet_green)
        setColor(R.color.gamification_green)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      12 -> {
        setImage(R.drawable.gamification_unknown_planet_brown)
        setColor(R.color.gamification_brown)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      13 -> {
        setImage(R.drawable.gamification_unknown_planet_blue)
        setColor(R.color.gamification_light_blue)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      14 -> {
        setImage(R.drawable.gamification_unknown_planet_red)
        setColor(R.color.gamification_dark_red)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      else -> {
        setImage(R.drawable.gamification_unknown_planet_purple)
        setColor(R.color.gamification_purple)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
    }
  }

  private fun handleToogleButton(level: Int) {
    if (level != 0) {
      itemView.toogle_button.setOnCheckedChangeListener { _, isChecked ->
        uiEventListener.onNext(isChecked)
      }
    } else {
      itemView.toogle_button.visibility = View.GONE
    }
  }

  private fun setImage(@DrawableRes image: Int) {
    itemView.current_level_image.setImageDrawable(
        ResourcesCompat.getDrawable(context.resources, image, null))
  }

  private fun setColor(@ColorRes color: Int) {
    val resColor = ResourcesCompat.getColor(context.resources, color, null)
    val ovalBackground =
        ResourcesCompat.getDrawable(context.resources, R.drawable.oval_grey_background, null)
    ovalBackground?.let {
      DrawableCompat.setTint(it.mutate(), resColor)
      itemView.current_level_bonus.background = it
    }
    itemView.current_level_progress_bar.progressTintList = ColorStateList.valueOf(resColor)
  }

  private fun setText(@StringRes title: Int, @StringRes progress: Int,
                      @StringRes phrase: Int, progressPercentage: String,
                      bonus: Double) {
    itemView.current_level_title.text = context.getString(title)
    itemView.spend_amount_text.text =
        "Spend ${getRemainingAmount()} more AppCoins to reach the next level"
    itemView.current_level_phrase.text =
        "All truths are easy to understand once they are discovered, the point is to discover them - Galileo Galilei"
    itemView.current_level_bonus.text = "$bonus% Bonus"
    itemView.percentage_left.text = "$progressPercentage%"
  }

  private fun getRemainingAmount() =
      currencyFormatUtils.formatGamificationValues(nextLevelAmount - amountSpent)

  private fun getProgressPercentage(levelAmount: BigDecimal): BigDecimal {
    var levelRange = nextLevelAmount - levelAmount
    if (levelRange.toDouble() == 0.0) {
      levelRange = BigDecimal.ONE
    }
    val amountSpentInLevel = amountSpent - levelAmount
    return amountSpentInLevel.divide(levelRange, 2, RoundingMode.HALF_EVEN)
        .multiply(BigDecimal(100))
  }

  private fun setProgress(progress: BigDecimal) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      itemView.current_level_progress_bar.setProgress(progress.toInt(), true)
    } else {
      itemView.current_level_progress_bar.progress = progress.toInt()
    }
  }
}