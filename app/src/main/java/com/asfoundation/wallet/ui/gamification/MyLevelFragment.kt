package com.asfoundation.wallet.ui.gamification

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_gamification_my_level.*
import kotlinx.android.synthetic.main.fragment_rewards_level.*
import kotlinx.android.synthetic.main.rewards_progress_bar.*
import javax.inject.Inject

class MyLevelFragment : DaggerFragment(), MyLevelView {
  @Inject
  lateinit var gamificationInteractor: GamificationInteractor
  @Inject
  lateinit var analytics: GamificationAnalytics


  private lateinit var presenter: MyLevelPresenter
  private lateinit var gamificationView: GamificationView
  private lateinit var howItWorksBottomSheet: BottomSheetBehavior<View>
  private var step = 100

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        MyLevelPresenter(this, gamificationView, gamificationInteractor, analytics, Schedulers.io(),
            AndroidSchedulers.mainThread())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context !is GamificationView) {
      throw IllegalArgumentException(
          HowItWorksFragment::class.java.simpleName + " needs to be attached to a " + GamificationView::class.java.simpleName)
    }
    gamificationView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    childFragmentManager.beginTransaction()
        .replace(R.id.gamification_fragment_container, HowItWorksFragment())
        .commit()
    return inflater.inflate(R.layout.fragment_gamification_my_level, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    howItWorksBottomSheet =
        BottomSheetBehavior.from(gamification_fragment_container)
    presenter.present(savedInstanceState)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun setupLayout() {
    for (i in 0..4) {
      gamification_progress_bar.setLevelIcons(i)
    }
  }

  override fun updateLevel(userStatus: UserRewardsStatus) {
    if (userStatus.bonus.size != 1) {
      step = 100 / (userStatus.bonus.size - 1)
    }
    gamification_loading.visibility = View.GONE

    setLevelResources(userStatus.level)

    gamification_progress_bar.animateProgress(userStatus.lastShownLevel, userStatus.level, step)

    if (userStatus.level > userStatus.lastShownLevel) {
      levelUpAnimation(userStatus.level)
    }

    for (value in userStatus.bonus) {
      val level = userStatus.bonus.indexOf(value)
      val isCurrentLevel = level == userStatus.level
      val bonusLabel = if (isCurrentLevel) {
        R.string.gamification_level_bonus
      } else {
        R.string.gamification_how_table_b2
      }
      gamification_progress_bar.setLevelBonus(level,
          getString(bonusLabel, gamification_progress_bar.formatLevelInfo(value)))
    }
  }

  override fun setStaringLevel(userStatus: UserRewardsStatus) {
    progress_bar.progress = userStatus.lastShownLevel * (100 / (userStatus.bonus.size - 1))
    levelUpAnimation(userStatus.level)
    for (i in 0..userStatus.lastShownLevel) {
      gamification_progress_bar.showPreviousLevelIcons(i, i < userStatus.lastShownLevel)
    }
  }

  override fun animateBackgroundFade() {
    howItWorksBottomSheet.setBottomSheetCallback(object :
        BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) {
      }

      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        background_fade_animation.progress = slideOffset
      }
    })
  }

  private fun setLevelResources(level: Int) {
    levelIdleAnimation(level)
    level_title.text = getString(R.string.gamification_level_header,
        getString(LevelResourcesMapper.mapTitle(level)))
    level_title.visibility = View.VISIBLE
    level_description.text = getString(LevelResourcesMapper.mapSubtitle(level))
    level_description.visibility = View.VISIBLE
    current_level.text =
        getString(R.string.gamification_level_on_graphic, (level + 1).toString())
  }

  private fun setLevelIdleAnimation(level: Int) {
    when (level) {
      0 -> gamification_current_level_animation.setMinAndMaxFrame(30, 150)
      1 -> gamification_current_level_animation.setMinAndMaxFrame(210, 330)
      2 -> gamification_current_level_animation.setMinAndMaxFrame(390, 510)
      3 -> gamification_current_level_animation.setMinAndMaxFrame(570, 690)
      4 -> gamification_current_level_animation.setMinAndMaxFrame(750, 870)
    }
  }

  private fun setLevelTransitionAnimation(toLevel: Int) {
    when (toLevel) {
      0 -> gamification_current_level_animation.setMinAndMaxFrame(0, 30)
      1 -> gamification_current_level_animation.setMinAndMaxFrame(30, 210)
      2 -> gamification_current_level_animation.setMinAndMaxFrame(210, 390)
      3 -> gamification_current_level_animation.setMinAndMaxFrame(390, 570)
      4 -> gamification_current_level_animation.setMinAndMaxFrame(570, 750)
    }
  }

  private fun levelUpAnimation(level: Int) {
    setLevelTransitionAnimation(level)
    gamification_current_level_animation.visibility = View.VISIBLE
    gamification_current_level_animation.playAnimation()
    gamification_current_level_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
        setLevelIdleAnimation(level)
      }

      override fun onAnimationEnd(animation: Animator?) {
      }

      override fun onAnimationCancel(animation: Animator?) {
      }

      override fun onAnimationStart(animation: Animator?) {
      }
    })
  }

  private fun levelIdleAnimation(level: Int) {
    setLevelIdleAnimation(level)
    gamification_current_level_animation.visibility = View.VISIBLE
    gamification_current_level_animation.playAnimation()
  }

  private fun expandBottomSheet() {
    howItWorksBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
  }

  private fun collapseBottomSheet() {
    howItWorksBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
  }

  override fun changeBottomSheetState() {
    if (howItWorksBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
      expandBottomSheet()
    } else {
      collapseBottomSheet()
    }
  }
}