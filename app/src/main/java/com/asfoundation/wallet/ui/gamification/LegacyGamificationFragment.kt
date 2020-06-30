package com.asfoundation.wallet.ui.gamification

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.asfoundation.wallet.promotions.GamificationProgressBarView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_gamification_legacy.*
import kotlinx.android.synthetic.main.fragment_rewards_level.*
import kotlinx.android.synthetic.main.rewards_progress_bar.view.*
import kotlinx.android.synthetic.main.rewards_progress_normal.*
import kotlinx.android.synthetic.main.rewards_progress_pioneer.*
import javax.inject.Inject

class LegacyGamificationFragment : DaggerFragment(), LegacyGamificationView {
  @Inject
  lateinit var gamificationInteractor: GamificationInteractor

  @Inject
  lateinit var analytics: GamificationAnalytics


  private lateinit var presenter: LegacyGamificationPresenter
  private lateinit var rewardsLevelView: RewardsLevelView
  private lateinit var howItWorksBottomSheet: BottomSheetBehavior<View>
  private lateinit var gamificationProgressBarView: GamificationProgressBarView
  private var step = 100

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter =
        LegacyGamificationPresenter(this, rewardsLevelView, gamificationInteractor, analytics,
            CompositeDisposable(), Schedulers.io(), AndroidSchedulers.mainThread())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is RewardsLevelView) { LegacyGamificationFragment::class.java.simpleName + " needs to be attached to a " + RewardsLevelView::class.java.simpleName }
    rewardsLevelView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    childFragmentManager.beginTransaction()
        .replace(R.id.gamification_fragment_container, HowItWorksFragment.newInstance())
        .commit()
    return inflater.inflate(R.layout.fragment_gamification_legacy, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    howItWorksBottomSheet =
        BottomSheetBehavior.from(gamification_fragment_container)
    gamificationProgressBarView = gamification_progress_bar_normal
    presenter.present(savedInstanceState)
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun showPioneerUser() {
    gamification_loading.visibility = View.GONE
    content.visibility = View.VISIBLE
    gamificationProgressBarView = gamification_progress_bar_pioneer
    gamificationProgressBarView.setupPioneerUi()
    rewards_layout_pioneer.visibility = View.VISIBLE
    rewards_layout_normal.visibility = View.GONE
  }

  override fun setLevelIcons() {
    for (i in 0..4) {
      gamificationProgressBarView.setLevelIcons(i)
    }
  }

  override fun updateLevel(lastShownLevel: Int, level: Int, bonus: List<Double>) {
    if (bonus.size != 1) {
      step = 100 / (bonus.size - 1)
    }

    setLevelResources(level)
    gamificationProgressBarView.animateProgress(lastShownLevel, level, step)

    if (level > lastShownLevel) levelUpAnimation(level)

    for (value in bonus) {
      val levelIndex = bonus.indexOf(value)
      val isCurrentLevel = levelIndex == level
      val bonusLabel = if (isCurrentLevel) {
        R.string.gamification_level_bonus
      } else {
        R.string.gamification_how_table_b2
      }

      gamificationProgressBarView.setLevelBonus(levelIndex,
          getString(bonusLabel, gamificationProgressBarView.formatLevelInfo(value)))
    }
  }

  override fun setStaringLevel(lastShownLevel: Int, level: Int, bonus: List<Double>) {

    gamificationProgressBarView.progress_bar.progress = lastShownLevel * (100 / (bonus.size - 1))
    levelUpAnimation(level)
    for (i in 0..lastShownLevel) {
      gamificationProgressBarView.showPreviousLevelIcons(i, i < lastShownLevel)
    }
  }

  override fun animateBackgroundFade() {
    howItWorksBottomSheet.addBottomSheetCallback(object :
        BottomSheetBehavior.BottomSheetCallback() {
      override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
      override fun onSlide(bottomSheet: View, slideOffset: Float) {
        background_fade_animation?.progress = slideOffset
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
      0 -> gamification_current_level_animation?.setMinAndMaxFrame(30, 150)
      1 -> gamification_current_level_animation?.setMinAndMaxFrame(210, 330)
      2 -> gamification_current_level_animation?.setMinAndMaxFrame(390, 510)
      3 -> gamification_current_level_animation?.setMinAndMaxFrame(570, 690)
      4 -> gamification_current_level_animation?.setMinAndMaxFrame(750, 870)
    }
  }

  private fun setLevelTransitionAnimation(toLevel: Int) {
    when (toLevel) {
      0 -> gamification_current_level_animation?.setMinAndMaxFrame(0, 30)
      1 -> gamification_current_level_animation?.setMinAndMaxFrame(30, 210)
      2 -> gamification_current_level_animation?.setMinAndMaxFrame(210, 390)
      3 -> gamification_current_level_animation?.setMinAndMaxFrame(390, 570)
      4 -> gamification_current_level_animation?.setMinAndMaxFrame(570, 750)
    }
  }

  private fun levelUpAnimation(level: Int) {
    setLevelTransitionAnimation(level)
    gamification_current_level_animation.visibility = View.VISIBLE
    gamification_current_level_animation.playAnimation()
    gamification_current_level_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) = setLevelIdleAnimation(level)
      override fun onAnimationEnd(animation: Animator?) = Unit
      override fun onAnimationCancel(animation: Animator?) = Unit
      override fun onAnimationStart(animation: Animator?) = Unit
    })
  }

  private fun levelIdleAnimation(level: Int) {
    setLevelIdleAnimation(level)
    gamification_current_level_animation.visibility = View.VISIBLE
    gamification_current_level_animation.playAnimation()
  }

  override fun changeBottomSheetState() {
    if (howItWorksBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
      howItWorksBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    } else if (howItWorksBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
      howItWorksBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    }
  }
}