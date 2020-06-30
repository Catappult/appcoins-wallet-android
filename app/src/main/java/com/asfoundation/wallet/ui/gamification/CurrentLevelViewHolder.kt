package com.asfoundation.wallet.ui.gamification

import android.animation.ObjectAnimator
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
        setBackground(R.drawable.gamification_background)
        setColor(R.color.gamification_green)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      1 -> {
        setBackground(R.drawable.referrals_background)
        setColor(R.color.gamification_light_grey)
        setText(R.string.gamification_a_galaxy_title, R.string.gamification_galaxy_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      2 -> {
        setBackground(R.drawable.gamification_background)
        setColor(R.color.gamification_red)
        setText(R.string.gamification_a_moon_title, R.string.gamification_a_moon_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      3 -> {
        setBackground(R.drawable.referrals_background)
        setColor(R.color.gamification_blue_grey)
        setText(R.string.gamification_a_planet_title, R.string.gamification_planet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      4 -> {
        setBackground(R.drawable.gamification_background)
        setColor(R.color.gamification_orange)
        setText(R.string.gamification_a_star_title, R.string.gamification_star_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      5 -> {
        setBackground(R.drawable.referrals_background)
        setColor(R.color.gamification_dark_yellow)
        setText(R.string.gamification_a_star_title, R.string.gamification_star_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      6 -> {
        setBackground(R.drawable.gamification_background)
        setColor(R.color.gamification_yellow)
        setText(R.string.gamification_a_planet_title, R.string.gamification_planet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      7 -> {
        setBackground(R.drawable.referrals_background)
        setColor(R.color.gamification_blue_green)
        setText(R.string.gamification_a_moon_title, R.string.gamification_a_moon_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      8 -> {
        setBackground(R.drawable.gamification_background)
        setColor(R.color.gamification_light_blue)
        setText(R.string.gamification_a_galaxy_title, R.string.gamification_galaxy_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
      9 -> {
        setBackground(R.drawable.referrals_background)
        setColor(R.color.gamification_blue)
        setText(R.string.gamification_a_comet_title, R.string.gamification_a_comet_subtitle,
            R.string.gamification_how_terms_and_conditions, progressPercentage, bonus)
      }
    }
  }

  private fun handleToogleButton(level: Int) {
    if (level != 0) {
      val arrow = itemView.toogle_button.compoundDrawablesRelative[2]
      itemView.toogle_button.setOnCheckedChangeListener { _, isChecked ->
        uiEventListener.onNext(isChecked)
        arrow?.let {
          ObjectAnimator.ofInt(it, "level", 0, 10000)
              .start()
        }
      }
    } else {
      itemView.toogle_button.visibility = View.GONE
    }
  }

  private fun setBackground(@DrawableRes image: Int) {
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
    itemView.current_level_phrase.text = context.getString(phrase)
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
    return amountSpentInLevel.divide(levelRange, 0, RoundingMode.HALF_EVEN)
  }

  private fun setProgress(progress: BigDecimal) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      itemView.current_level_progress_bar.setProgress(progress.toInt(), true)
    } else {
      itemView.current_level_progress_bar.progress = progress.toInt()
    }
  }
}