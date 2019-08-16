package com.asfoundation.wallet.promotions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.asf.wallet.R
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.LevelResourcesMapper
import com.asfoundation.wallet.ui.gamification.ProgressAnimation
import com.asfoundation.wallet.ui.gamification.UserRewardsStatus
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.level_component.view.*
import kotlinx.android.synthetic.main.rewards_progress_bar.*
import javax.inject.Inject

class PromotionsFragment : DaggerFragment(), PromotionsView {

  @Inject
  lateinit var gamification: GamificationInteractor
  @Inject
  lateinit var levelResourcesMapper: LevelResourcesMapper
  private var step = 100


  private lateinit var presenter: PromotionsPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = PromotionsPresenter(this, gamification, CompositeDisposable(), Schedulers.io(),
        AndroidSchedulers.mainThread())
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.promotions_fragment_view, container, false)
  }

  override fun setupLayout() {
    for (i in 0..4) {
      setLevelIcons(i)
    }
  }

  override fun setStaringLevel(userStatus: UserRewardsStatus) {
    progress_bar.progress = userStatus.lastShownLevel * (100 / (userStatus.bonus.size - 1))
    for (i in 0..userStatus.lastShownLevel) {
      showPreviousLevelIcons(i, i < userStatus.lastShownLevel)
    }
  }

  override fun updateLevel(userStatus: UserRewardsStatus) {
    if (userStatus.bonus.size != 1) {
      step = 100 / (userStatus.bonus.size - 1)
    }

    animateProgress(userStatus.lastShownLevel, userStatus.level)

    for (value in userStatus.bonus) {
      val level = userStatus.bonus.indexOf(value)
      setLevelBonus(level, formatLevelInfo(value), level == userStatus.lastShownLevel)
    }
  }

  private fun showPreviousLevelIcons(level: Int, shouldHideLabel: Boolean) {
    when (level) {
      0 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_1, level_1_text)
      }
      1 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_2, level_2_text)
      }
      2 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_3, level_3_text)
      }
      3 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_4, level_4_text)
      }
      4 -> {
        noAnimationLevelUpdate(level, shouldHideLabel, level_5, level_5_text)
      }
    }
  }

  private fun noAnimationLevelUpdate(level: Int, shouldHideLabel: Boolean, iconView: View,
                                     labelView: TextView) {
    iconView.level_inactive_icon.setImageResource(levelResourcesMapper.mapIcons(level))
    iconView.level_inactive_icon.visibility = View.VISIBLE
    if (shouldHideLabel) {
      labelView.visibility = View.INVISIBLE
    } else {
      labelView.isEnabled = true
    }
  }

  private fun setLevelIcons(level: Int) {
    when (level) {
      0 -> level_1.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
      1 -> level_2.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
      2 -> level_3.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
      3 -> level_4.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
      4 -> level_5.level_active_icon.setImageResource(levelResourcesMapper.mapIcons(level))
    }
  }

  private fun setLevelBonus(level: Int, text: String, isCurrentLevel: Boolean) {
    val bonusLabel = if (isCurrentLevel) {
      R.string.gamification_level_bonus
    } else {
      R.string.gamification_how_table_b2
    }
    when (level) {
      0 -> level_1_text.text = getString(bonusLabel, text)
      1 -> level_2_text.text = getString(bonusLabel, text)
      2 -> level_3_text.text = getString(bonusLabel, text)
      3 -> level_4_text.text = getString(bonusLabel, text)
      4 -> level_5_text.text = getString(bonusLabel, text)
    }
  }

  private fun animateProgress(fromLevel: Int, toLevel: Int) {
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
      override fun onAnimationRepeat(animation: Animation?) {
      }

      override fun onAnimationEnd(animation: Animation?) {
        if (fromLevel <= nextLevel) {
          levelUp(nextLevel)
        } else {
          levelReUp(nextLevel)
        }
        animateProgress(fromLevel + 1, toLevel)
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
      override fun onAnimationRepeat(animation: Animation?) {
      }

      override fun onAnimationEnd(animation: Animation?) {
        activeIcon.visibility = View.VISIBLE
        levelText.isEnabled = true
        levelText.visibility = View.VISIBLE
      }

      override fun onAnimationStart(animation: Animation?) {
      }
    }
    if (newLevel) startBounceAnimation(activeIcon, listener) else startRebounceAnimation(activeIcon,
        listener)
  }

  private fun animateLevelToLock(levelIcon: View, levelText: TextView) {
    val icon = levelIcon.findViewById(R.id.level_active_icon) as ImageView
    startShrinkAnimation(icon)
    icon.visibility = View.INVISIBLE
    levelText.isEnabled = false
    levelText.visibility = View.VISIBLE
  }

  private fun animateLevelShow(levelIcon: View, levelText: TextView) {
    val activeIcon = levelIcon.findViewById(R.id.level_active_icon) as ImageView
    startGrowAnimation(activeIcon)
    levelText.isEnabled = false
    levelText.visibility = View.INVISIBLE
  }

  private fun startBounceAnimation(view: View, listener: Animation.AnimationListener) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.bounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun startRebounceAnimation(view: View, listener: Animation.AnimationListener) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.rebounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun startShrinkAnimation(view: View) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.shrink_animation)
    animation.fillAfter = true
    view.startAnimation(animation)
  }

  private fun startGrowAnimation(view: View) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.grow_animation)
    animation.fillAfter = true
    view.startAnimation(animation)
  }


  private fun formatLevelInfo(value: Double): String {
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
