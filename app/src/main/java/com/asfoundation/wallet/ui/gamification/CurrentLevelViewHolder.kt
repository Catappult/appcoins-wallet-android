package com.asfoundation.wallet.ui.gamification

import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.GAMIFICATION_INFO_ID
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.SHOW_REACHED_LEVELS_ID
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.current_level_card.view.*
import kotlinx.android.synthetic.main.current_level_layout.view.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat


class CurrentLevelViewHolder(
  itemView: View,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val mapper: GamificationMapper,
  private val uiEventListener: PublishSubject<Pair<String, Boolean>>
) :
  LevelsViewHolder(itemView) {

  override fun bind(level: LevelItem) {
    val currentLevel = level as CurrentLevelItem
    val progress = getProgressPercentage(
      currentLevel.amount, currentLevel.amountSpent,
      currentLevel.nextLevelAmount
    )
    val progressString = validateAndGetProgressString(progress)
    handleSpecificLevel(
      currentLevel.level, progressString, currentLevel.bonus,
      currentLevel.amountSpent, currentLevel.nextLevelAmount
    )
    setProgress(progress)
    handleToggleButton(currentLevel.level)
    itemView.gamification_info_btn.setOnClickListener {
      uiEventListener.onNext(Pair(GAMIFICATION_INFO_ID, true))
    }
  }

  private fun handleSpecificLevel(
    level: Int, progressPercentage: String, bonus: Double,
    amountSpent: BigDecimal, nextLevelAmount: BigDecimal?
  ) {
    val currentLevelInfo = mapper.mapCurrentLevelInfo(level)
    itemView.current_level_image.setImageDrawable(currentLevelInfo.planet)
    setColor(currentLevelInfo.levelColor)
    setText(
      currentLevelInfo.title, currentLevelInfo.phrase, progressPercentage, bonus, amountSpent,
      nextLevelAmount
    )
  }

  private fun handleToggleButton(level: Int) {
    if (level != 0) {
      itemView.toggle_button.visibility = View.VISIBLE
      itemView.toggle_button.setOnCheckedChangeListener { _, isChecked ->
        uiEventListener.onNext(Pair(SHOW_REACHED_LEVELS_ID, isChecked))
      }
    } else {
      itemView.toggle_button.visibility = View.GONE
    }
  }

  private fun setColor(color: Int) {
    itemView.current_level_bonus.background = mapper.getOvalBackground(color)
    itemView.current_level_progress_bar.progressTintList = ColorStateList.valueOf(color)
  }

  private fun setText(
    title: String, phrase: String, progressPercentage: String,
    bonus: Double, amountSpent: BigDecimal, nextLevelAmount: BigDecimal?
  ) {
    itemView.current_level_title.text = title
    if (nextLevelAmount != null) {
      itemView.spend_amount_text.text =
        itemView.context.getString(
          R.string.gamif_card_body,
          currencyFormatUtils.formatGamificationValues(nextLevelAmount - amountSpent)
        )
    } else {
      itemView.spend_amount_text.visibility = View.INVISIBLE
    }
    itemView.current_level_phrase.text = phrase
    val df = DecimalFormat("###.#")
    itemView.current_level_bonus.text =
      itemView.context.getString(R.string.gamif_bonus, df.format(bonus))
    itemView.percentage_left.text = "$progressPercentage%"
  }

  private fun getProgressPercentage(
    levelAmount: BigDecimal, amountSpent: BigDecimal,
    nextLevelAmount: BigDecimal?
  ): BigDecimal {
    return if (nextLevelAmount != null) {
      var levelRange = nextLevelAmount.subtract(levelAmount)
      if (levelRange.toDouble() == 0.0) {
        levelRange = BigDecimal.ONE
      }
      val amountSpentInLevel = amountSpent.subtract(levelAmount)
      amountSpentInLevel.divide(levelRange, 2, RoundingMode.DOWN)
        .multiply(BigDecimal(100))
    } else {
      BigDecimal(100)
    }
  }

  private fun setProgress(progress: BigDecimal) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      itemView.current_level_progress_bar.setProgress(progress.toInt(), true)
    } else {
      itemView.current_level_progress_bar.progress = progress.toInt()
    }
  }

  private fun validateAndGetProgressString(progress: BigDecimal): String {
    return if (progress >= BigDecimal.ZERO && progress <= BigDecimal(100.0)) {
      currencyFormatUtils.formatGamificationValues(progress)
    } else {
      ""
    }
  }
}