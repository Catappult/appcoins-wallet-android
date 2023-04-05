package com.asfoundation.wallet.ui.gamification

import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.GAMIFICATION_INFO_ID
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.SHOW_REACHED_LEVELS_ID
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.databinding.CurrentLevelLayoutBinding
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import java.text.DecimalFormat


class CurrentLevelViewHolder(
  itemView: View,
  private val currencyFormatUtils: CurrencyFormatUtils,
  private val mapper: GamificationMapper,
  private val uiEventListener: PublishSubject<Pair<String, Boolean>>
) :
  LevelsViewHolder(itemView) {

  private val binding by lazy { CurrentLevelLayoutBinding.bind(itemView) }

  // current_level_layout.xml
  private val toggle_button get() = binding.toggleButton

  // current_level_card.xml
  private val gamification_info_btn get() = binding.currentLevelCardLayout.gamificationInfoBtn
  private val current_level_image get() = binding.currentLevelCardLayout.currentLevelImage
  private val current_level_bonus get() = binding.currentLevelCardLayout.currentLevelBonus
  private val current_level_progress_bar get() = binding.currentLevelCardLayout.currentLevelProgressBar
  private val current_level_title get() = binding.currentLevelCardLayout.currentLevelTitle
  private val spend_amount_text get() = binding.currentLevelCardLayout.spendAmountText
  private val current_level_phrase get() = binding.currentLevelCardLayout.currentLevelPhrase
  private val percentage_left get() = binding.currentLevelCardLayout.percentageLeft

  override fun bind(level: LevelItem) {
    val currentLevel = level as CurrentLevelItem
    val progress = mapper.getProgressPercentage(currentLevel.amountSpent, currentLevel.nextLevelAmount)
    val progressString = mapper.validateAndGetProgressString(
      currentLevel.amountSpent,
      currentLevel.nextLevelAmount
    )
    handleSpecificLevel(
      currentLevel.level, progressString, currentLevel.bonus,
      currentLevel.amountSpent, currentLevel.nextLevelAmount
    )
    setProgress(progress)
    handleToggleButton(currentLevel.level)
    gamification_info_btn.setOnClickListener {
      uiEventListener.onNext(Pair(GAMIFICATION_INFO_ID, true))
    }
  }

  private fun handleSpecificLevel(
    level: Int, progressString: String, bonus: Double,
    amountSpent: BigDecimal, nextLevelAmount: BigDecimal?
  ) {
    val currentLevelInfo = mapper.mapCurrentLevelInfo(level)
    current_level_image.setImageDrawable(currentLevelInfo.planet)
    setColor(currentLevelInfo.levelColor)
    setText(
      currentLevelInfo.title, currentLevelInfo.phrase, progressString, bonus, amountSpent,
      nextLevelAmount
    )
  }

  private fun handleToggleButton(level: Int) {
    if (level != 0) {
      toggle_button.visibility = View.VISIBLE
      toggle_button.setOnCheckedChangeListener { _, isChecked ->
        uiEventListener.onNext(Pair(SHOW_REACHED_LEVELS_ID, isChecked))
      }
    } else {
      toggle_button.visibility = View.GONE
    }
  }

  private fun setColor(color: Int) {
    current_level_bonus.background = mapper.getOvalBackground(color)
    current_level_progress_bar.progressTintList = ColorStateList.valueOf(color)
  }

  private fun setText(
    title: String, phrase: String, progressPercentage: String,
    bonus: Double, amountSpent: BigDecimal, nextLevelAmount: BigDecimal?
  ) {
    current_level_title.text = title
    if (nextLevelAmount != null) {
      spend_amount_text.text =
        itemView.context.getString(
          R.string.gamif_card_body,
          currencyFormatUtils.formatGamificationValues(nextLevelAmount - amountSpent)
        )
    } else {
      spend_amount_text.visibility = View.INVISIBLE
    }
    current_level_phrase.text = phrase
    val df = DecimalFormat("###.#")
    current_level_bonus.text =
      itemView.context.getString(R.string.gamif_bonus, df.format(bonus))
    percentage_left.text = progressPercentage
  }

  private fun setProgress(progress: BigDecimal) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      current_level_progress_bar.setProgress(progress.toInt(), true)
    } else {
      current_level_progress_bar.progress = progress.toInt()
    }
  }
}