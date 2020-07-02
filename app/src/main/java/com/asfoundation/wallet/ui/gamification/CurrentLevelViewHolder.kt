package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
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
import java.text.DecimalFormat


class CurrentLevelViewHolder(itemView: View,
                             private val context: Context,
                             private val amountSpent: BigDecimal,
                             private val nextLevelAmount: BigDecimal,
                             private val currencyFormatUtils: CurrencyFormatUtils,
                             private val mapper: GamificationMapper,
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
    val currentLevelInfo = mapper.mapCurrentLevelInfo(level)
    itemView.current_level_image.setImageDrawable(currentLevelInfo.planet)
    setColor(currentLevelInfo.levelColor)
    setText(currentLevelInfo.title, currentLevelInfo.phrase, progressPercentage, bonus)
  }

  private fun handleToogleButton(level: Int) {
    if (level != 0) {
      itemView.toggle_button.setOnCheckedChangeListener { _, isChecked ->
        uiEventListener.onNext(isChecked)
      }
    } else {
      itemView.toggle_button.visibility = View.GONE
    }
  }

  private fun setColor(color: Int) {
    val ovalBackground =
        ResourcesCompat.getDrawable(context.resources, R.drawable.oval_grey_background, null)
    ovalBackground?.let {
      DrawableCompat.setTint(it.mutate(), color)
      itemView.current_level_bonus.background = it
    }
    itemView.current_level_progress_bar.progressTintList = ColorStateList.valueOf(color)
  }

  private fun setText(title: String, phrase: String, progressPercentage: String,
                      bonus: Double) {
    itemView.current_level_title.text = title
    itemView.spend_amount_text.text =
        context.getString(R.string.gamif_card_body, getRemainingAmount())
    itemView.current_level_phrase.text = phrase
    val df = DecimalFormat("###.#")
    itemView.current_level_bonus.text =
        context.getString(R.string.gamification_level_bonus, df.format(bonus))
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