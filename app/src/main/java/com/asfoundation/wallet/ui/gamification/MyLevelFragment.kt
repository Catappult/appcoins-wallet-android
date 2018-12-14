package com.asfoundation.wallet.ui.gamification

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_rewards_level.*
import kotlinx.android.synthetic.main.level_component.view.*
import kotlinx.android.synthetic.main.rewards_progress_bar.*
import java.math.BigDecimal
import javax.inject.Inject

class MyLevelFragment : DaggerFragment(), MyLevelView {
  @Inject
  lateinit var gamificationInteractor: GamificationInteractor
  @Inject
  lateinit var levelResourcesMapper: LevelResourcesMapper

  private lateinit var presenter: MyLevelPresenter
  private lateinit var gamificationView: GamificationView
  private var currentLevel = 0
  private var step = 100

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    presenter = MyLevelPresenter(this, gamificationInteractor, Schedulers.io(),
        AndroidSchedulers.mainThread())
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context !is GamificationView) {
      throw IllegalArgumentException(
          HowItWorksFragment::class.java.simpleName + " needs to be attached to a " + GamificationView::class.java.simpleName)
    }
    gamificationView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_rewards_level, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun setupLayout() {
    setLevelIcons(0)
    setLevelIcons(1)
    setLevelIcons(2)
    setLevelIcons(3)
    setLevelIcons(4)
  }

  override fun updateLevel(userStatus: UserRewardsStatus) {
    step = 100 / (userStatus.bonus.size - 1)

    setLevelResources(userStatus.level)

    if (userStatus.toNextLevelAmount > BigDecimal.ZERO) {
      earned_label.visibility = View.VISIBLE
      earned_value.text =
          getString(R.string.gamification_level_next_lvl_value, userStatus.toNextLevelAmount)
      earned_value.visibility = View.VISIBLE
    } else {
      earned_label.visibility = View.INVISIBLE
      earned_value.visibility = View.INVISIBLE
    }
    animateProgress(currentLevel, userStatus.level)

    for (value in userStatus.bonus) {
      setLevelBonus(userStatus.bonus.indexOf(value), value.toString())
    }

  }

  override fun getButtonClicks(): Observable<Any> {
    return RxView.clicks(details_button)
  }

  override fun showHowItWorksScreen() {
    gamificationView.showHowItWorksView()
  }

  override fun showHowItWorksButton() {
    details_button.visibility = View.VISIBLE
    gamificationView.showHowItWorksButton()
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
        progress_bar?.progress!!.toFloat(), to.toFloat())
    animation.duration = if (nextLevel == toLevel) 1000 else 600
    animation.setAnimationListener(object : AnimationListener {
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

  private fun animateLevelUp(levelIcon: View?, levelText: TextView?, newLevel: Boolean) {
    val activeIcon = levelIcon?.findViewById(R.id.level_active_icon) as ImageView
    val listener = object : AnimationListener {
      override fun onAnimationRepeat(animation: Animation?) {
      }

      override fun onAnimationEnd(animation: Animation?) {
        activeIcon.visibility = View.VISIBLE
        levelText?.isEnabled = true
        levelText?.visibility = View.VISIBLE
      }

      override fun onAnimationStart(animation: Animation?) {
      }
    }
    if (newLevel) startBounceAnimation(activeIcon, listener) else startRebounceAnimation(activeIcon,
        listener)
  }

  private fun animateLevelToLock(levelIcon: View?, levelText: TextView?) {
    var icon: ImageView = levelIcon?.findViewById(R.id.level_active_icon) as ImageView
    startShrinkAnimation(icon)
    icon.visibility = View.INVISIBLE
    levelText?.isEnabled = false
    levelText?.visibility = View.VISIBLE
  }

  private fun animateLevelShow(levelIcon: View?, levelText: TextView?) {
    val activeIcon = levelIcon?.findViewById(R.id.level_active_icon) as ImageView
    startGrowAnimation(activeIcon)
    levelText?.isEnabled = false
    levelText?.visibility = View.INVISIBLE
  }

  private fun startBounceAnimation(view: View?, listener: AnimationListener) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.bounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view?.startAnimation(animation)
  }

  private fun startRebounceAnimation(view: View?, listener: AnimationListener) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.rebounce_animation)
    animation.setAnimationListener(listener)
    animation.fillAfter = true
    view?.startAnimation(animation)
  }

  private fun startShrinkAnimation(view: View?) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.shrink_animation)
    animation.fillAfter = true
    view?.startAnimation(animation)
  }

  private fun startGrowAnimation(view: View?) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.grow_animation)
    animation.fillAfter = true
    view?.startAnimation(animation)
  }

  private fun setLevelResources(level: Int) {
    animateImageSwap(level_img, levelResourcesMapper.mapImage(level))
    level_title.text = getString(R.string.gamification_level_header,
        getString(levelResourcesMapper.mapTitle(level)))
    level_title.visibility = View.VISIBLE
    level_description.text = getString(levelResourcesMapper.mapSubtitle(level))
    level_description.visibility = View.VISIBLE
  }

  private fun setLevelBonus(level: Int, text: String) {
    when (level) {
      0 -> level_1_text.text = getString(R.string.gamification_level_bonus, text)
      1 -> level_2_text.text = getString(R.string.gamification_level_bonus, text)
      2 -> level_3_text.text = getString(R.string.gamification_level_bonus, text)
      3 -> level_4_text.text = getString(R.string.gamification_level_bonus, text)
      4 -> level_5_text.text = getString(R.string.gamification_level_bonus, text)
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

  private fun animateImageSwap(imageView: ImageView, newImage: Int) {
    if (imageView.visibility == View.VISIBLE) {
      fadeOutAnimation(imageView, object : AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
          imageView.setImageResource(newImage)
          fadeInAnimation(imageView, null)
        }

        override fun onAnimationStart(animation: Animation?) {
        }

      })
    } else {
      imageView.setImageResource(newImage)
      fadeInAnimation(imageView, null)
    }
  }

  private fun fadeOutAnimation(view: View, listener: AnimationListener?) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.fade_out_animation)
    animation.fillAfter = true
    animation.setAnimationListener(listener)
    view.startAnimation(animation)
  }

  private fun fadeInAnimation(view: View, listener: AnimationListener?) {
    val animation = AnimationUtils.loadAnimation(activity, R.anim.fade_in_animation)
    animation.fillAfter = true
    animation.setAnimationListener(listener)
    view.startAnimation(animation)
  }
}