package com.asfoundation.wallet.promotions

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.asf.wallet.R
import com.asfoundation.wallet.ui.gamification.LevelResourcesMapper
import com.asfoundation.wallet.ui.gamification.ProgressAnimation
import kotlinx.android.synthetic.main.level_component.view.*
import kotlinx.android.synthetic.main.rewards_progress_bar.view.*


class GamificationProgressBarView
@JvmOverloads
constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

  init {
    View.inflate(context, R.layout.rewards_progress_bar, this)
  }

  private fun setLevelUi(view: View, levelText: TextView) {
    view.level_active_icon.setBackgroundResource(R.drawable.level_icon_background_border)
    view.level_inactive_icon.setBackgroundResource(R.drawable.level_icon_background_border)
    levelText.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
  }

  fun setupPioneerUi() {
    setLevelUi(level_1, level_1_text)
    setLevelUi(level_2, level_2_text)
    setLevelUi(level_3, level_3_text)
    setLevelUi(level_4, level_4_text)
    setLevelUi(level_5, level_5_text)

    progress_bar.progressTintList =
        ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.white, null))
  }

  fun showPreviousLevelIcons(level: Int, shouldHideLabel: Boolean) {
    when (level) {
      0 -> noAnimationLevelUpdate(level, shouldHideLabel, level_1, level_1_text)
      1 -> noAnimationLevelUpdate(level, shouldHideLabel, level_2, level_2_text)
      2 -> noAnimationLevelUpdate(level, shouldHideLabel, level_3, level_3_text)
      3 -> noAnimationLevelUpdate(level, shouldHideLabel, level_4, level_4_text)
      4 -> noAnimationLevelUpdate(level, shouldHideLabel, level_5, level_5_text)
    }
  }

  private fun noAnimationLevelUpdate(level: Int, shouldHideLabel: Boolean, iconView: View,
                                     labelView: TextView) {
    iconView.level_inactive_icon.setImageResource(LevelResourcesMapper.mapIcons(level))
    iconView.level_inactive_icon.visibility = VISIBLE
    if (shouldHideLabel) {
      labelView.visibility = INVISIBLE
    } else {
      labelView.isEnabled = true
    }
  }

  fun setLevelIcons(level: Int) {
    when (level) {
      0 -> level_1.level_active_icon.setImageResource(LevelResourcesMapper.mapIcons(level))
      1 -> level_2.level_active_icon.setImageResource(LevelResourcesMapper.mapIcons(level))
      2 -> level_3.level_active_icon.setImageResource(LevelResourcesMapper.mapIcons(level))
      3 -> level_4.level_active_icon.setImageResource(LevelResourcesMapper.mapIcons(level))
      4 -> level_5.level_active_icon.setImageResource(LevelResourcesMapper.mapIcons(level))
    }
  }

  fun setLevelBonus(level: Int, levelText: String) {
    when (level) {
      0 -> level_1_text.text = levelText
      1 -> level_2_text.text = levelText
      2 -> level_3_text.text = levelText
      3 -> level_4_text.text = levelText
      4 -> level_5_text.text = levelText
    }
  }

  fun animateProgress(fromLevel: Int, toLevel: Int, step: Int) {
    if (fromLevel == toLevel) {
      if (toLevel == 0) {
        levelUp(0)
      }
      return
    }

    val nextLevel = fromLevel + 1
    val to = (nextLevel) * step
    val animation = ProgressAnimation(progress_bar,
        progress_bar.progress.toFloat(), to.toFloat())
    animation.duration = if (nextLevel == toLevel) 1000 else 600
    animation.setAnimationListener(object : Animation.AnimationListener {
      override fun onAnimationRepeat(animation: Animation?) = Unit
      override fun onAnimationEnd(animation: Animation?) {
        if (fromLevel <= nextLevel) {
          levelUp(nextLevel)
        } else {
          levelReUp(nextLevel)
        }
        animateProgress(fromLevel + 1, toLevel, step)
      }

      override fun onAnimationStart(animation: Animation?) {
        if (fromLevel <= nextLevel) {
          levelShow(fromLevel)
        } else {
          levelLock(fromLevel)
        }
      }

    })
    progress_bar.startAnimation(animation)
  }

  private fun levelUp(level: Int) {
    when (level) {
      0 -> animateLevelUp(level_1, level_1_text, true)
      1 -> animateLevelUp(level_2, level_2_text, true)
      2 -> animateLevelUp(level_3, level_3_text, true)
      3 -> animateLevelUp(level_4, level_4_text, true)
      4 -> animateLevelUp(level_5, level_5_text, true)

    }
  }

  private fun levelShow(level: Int) {
    when (level) {
      0 -> animateLevelShow(level_1, level_1_text)
      1 -> animateLevelShow(level_2, level_2_text)
      2 -> animateLevelShow(level_3, level_3_text)
      3 -> animateLevelShow(level_4, level_4_text)
      4 -> animateLevelShow(level_5, level_5_text)
    }
  }

  private fun levelLock(level: Int) {
    when (level) {
      0 -> animateLevelToLock(level_1, level_1_text)
      1 -> animateLevelToLock(level_2, level_2_text)
      2 -> animateLevelToLock(level_3, level_3_text)
      3 -> animateLevelToLock(level_4, level_4_text)
      4 -> animateLevelToLock(level_5, level_5_text)
    }
  }

  private fun levelReUp(level: Int) {
    when (level) {
      0 -> animateLevelUp(level_1, level_1_text, false)
      1 -> animateLevelUp(level_2, level_2_text, false)
      2 -> animateLevelUp(level_3, level_3_text, false)
      3 -> animateLevelUp(level_4, level_4_text, false)
      4 -> animateLevelUp(level_5, level_5_text, false)
    }
  }

  private fun animateLevelUp(levelIcon: View, levelText: TextView, newLevel: Boolean) {
    val activeIcon = levelIcon.findViewById(R.id.level_active_icon) as ImageView
    val listener = object : Animation.AnimationListener {
      override fun onAnimationRepeat(animation: Animation?) = Unit
      override fun onAnimationEnd(animation: Animation?) {
        activeIcon.visibility = VISIBLE
        levelText.isEnabled = true
        levelText.visibility = VISIBLE
      }

      override fun onAnimationStart(animation: Animation?) = Unit
    }
    if (newLevel) startBounceAnimation(activeIcon, listener)
    else startRebounceAnimation(activeIcon, listener)
  }

  private fun animateLevelToLock(levelIcon: View, levelText: TextView) {
    val icon = levelIcon.findViewById(R.id.level_active_icon) as ImageView
    startShrinkAnimation(icon)
    icon.visibility = INVISIBLE
    levelText.isEnabled = false
    levelText.visibility = VISIBLE
  }

  private fun animateLevelShow(levelIcon: View, levelText: TextView) {
    val activeIcon = levelIcon.findViewById(R.id.level_active_icon) as ImageView
    startGrowAnimation(activeIcon)
    levelText.isEnabled = false
    levelText.visibility = INVISIBLE
  }

  private fun startBounceAnimation(view: View, listener: Animation.AnimationListener) {
    val animation = AnimationUtils.loadAnimation(context, R.anim.bounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun startRebounceAnimation(view: View, listener: Animation.AnimationListener) {
    val animation = AnimationUtils.loadAnimation(context, R.anim.rebounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun startShrinkAnimation(view: View) {
    val animation = AnimationUtils.loadAnimation(context, R.anim.shrink_animation)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun startGrowAnimation(view: View) {
    val animation = AnimationUtils.loadAnimation(context, R.anim.grow_animation)
    animation.fillAfter = true
    view.startAnimation(animation)
  }


  fun formatLevelInfo(value: Double): String {
    val splitValue = value.toString()
        .split(".")
    return if (splitValue[1] != "0") {
      value.toString()
    } else {
      removeDecimalPlaces(value)
    }
  }

  private fun removeDecimalPlaces(value: Double): String {
    val splitValue = value.toString()
        .split(".")
    return splitValue[0]
  }
}
