package com.asfoundation.wallet.ui.gamification

import android.content.res.ColorStateList
import android.view.View
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.asf.wallet.R
import com.asf.wallet.databinding.CurrentLevelLayoutBinding
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.GAMIFICATION_INFO_ID
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.SHOW_REACHED_LEVELS_ID
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

  override fun bind(level: LevelItem) {
    val currentLevel = level as CurrentLevelItem
    val progress =
      mapper.getProgressPercentage(currentLevel.amountSpent, currentLevel.nextLevelAmount)
    handleSpecificLevel(
      currentLevel.level, currentLevel.bonus,
      currentLevel.amountSpent, currentLevel.nextLevelAmount, currentLevel.currency
    )
    setProgress(progress)
    handleToggleButton(currentLevel.level)
    binding.currentLevelCardLayout.gamificationInfoBtn.setOnClickListener {
      uiEventListener.onNext(Pair(GAMIFICATION_INFO_ID, true))
    }
  }

  private fun handleSpecificLevel(
    level: Int, bonus: Double,
    amountSpent: BigDecimal, nextLevelAmount: BigDecimal?, currency: String?
  ) {
    val currentLevelInfo = mapper.mapCurrentLevelInfo(level)
    binding.currentLevelCardLayout.currentLevelImage.setImageDrawable(currentLevelInfo.planet)
    setColor(currentLevelInfo.levelColor)
    setText(
      currentLevelInfo.title, currentLevelInfo.phrase, bonus, amountSpent,
      nextLevelAmount, currency
    )
  }

  private fun handleToggleButton(level: Int) {
    if (level != 0) {
      binding.toggleButton.visibility = View.VISIBLE
      binding.toggleButton.setOnCheckedChangeListener { _, isChecked ->
        uiEventListener.onNext(Pair(SHOW_REACHED_LEVELS_ID, isChecked))
      }
    } else {
      binding.toggleButton.visibility = View.GONE
    }
  }

  private fun setColor(color: Int) {
    binding.currentLevelCardLayout.currentLevelBonus.background = mapper.getOvalBackground(color)
    binding.currentLevelCardLayout.currentLevelProgressBar.progressTintList =
      ColorStateList.valueOf(color)
  }

  private fun setText(
    title: String, phrase: String,
    bonus: Double, amountSpent: BigDecimal, nextLevelAmount: BigDecimal?, currency: String?
  ) {
    binding.currentLevelCardLayout.currentLevelTitle.text = title
    if (nextLevelAmount != null) {
      binding.currentLevelCardLayout.spendAmountText.text =
        itemView.context.getString(
          R.string.gamif_card_body,
          currency.plus(currencyFormatUtils.formatGamificationValues(nextLevelAmount - amountSpent))
        )
    } else {
      binding.currentLevelCardLayout.spendAmountText.visibility = View.INVISIBLE
    }
    binding.currentLevelCardLayout.currentLevelPhrase.text = phrase
    val df = DecimalFormat("###.#")
    binding.currentLevelCardLayout.currentLevelBonus.text =
      itemView.context.getString(R.string.gamif_bonus, df.format(bonus))
  }

  private fun setProgress(progress: BigDecimal) {
    binding.currentLevelCardLayout.currentLevelProgressBar.setProgress(progress.toInt(), true)
  }
}