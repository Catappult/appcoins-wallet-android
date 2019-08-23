package com.asfoundation.wallet.ui.onboarding

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R
import com.rd.PageIndicatorView


class OnboardingPageChangeListener internal constructor(private val view: View) :
    ViewPager2.OnPageChangeCallback() {

  companion object {
    var ANIMATION_TRANSITIONS = 3
    var pageCount = 4
  }

  private var lottieView: LottieAnimationView? = null
  private var skipButton: Button? = null
  private var nextButton: Button? = null
  private var redeemBonus: Button? = null
  private var checkBox: CheckBox? = null
  private var warningText: TextView? = null
  private var termsConditionsLayout: LinearLayout? = null
  private var pageIndicatorView: PageIndicatorView? = null

  init {
    init()
  }

  fun init() {
    lottieView = view.findViewById(R.id.lottie_onboarding)
    skipButton = view.findViewById(R.id.skip_button)
    nextButton = view.findViewById(R.id.next_button)
    checkBox = view.findViewById(R.id.onboarding_checkbox)
    redeemBonus = view.findViewById(R.id.redeem_bonus)
    warningText = view.findViewById(R.id.terms_conditions_warning)
    termsConditionsLayout = view.findViewById(R.id.terms_conditions_layout)
    pageIndicatorView = view.findViewById(R.id.page_indicator)
    updatePageIndicator(0)
  }

  private fun animateCheckboxUp(layout: LinearLayout) {
    (AnimatorInflater.loadAnimator(view.context,
        R.animator.minor_translate_up) as AnimatorSet).apply {
      setTarget(layout)
      start()
    }
  }

  private fun animateCheckboxDown(layout: LinearLayout) {
    (AnimatorInflater.loadAnimator(view.context,
        R.animator.minor_translate_down) as AnimatorSet).apply {
      setTarget(layout)
      start()
    }
  }

  private fun animateShowButton(button: Button) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.bottom_translate_in)
    button.animation = animation
  }

  private fun animateShowWarning(textView: TextView) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.fast_fade_in_animation)
    textView.animation = animation
  }

  private fun animateHideButton(button: Button) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.bottom_translate_out)
    button.animation = animation
  }

  private fun animateHideWarning(textView: TextView) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.fast_fade_out_animation)
    textView.animation = animation
  }

  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    lottieView!!.progress =
        position * (1f / ANIMATION_TRANSITIONS) + positionOffset * (1f / ANIMATION_TRANSITIONS)
    checkBox!!.setOnClickListener { handleUI(position) }
    updatePageIndicator(position)
    handleUI(position)
  }

  private fun handleUI(position: Int) {
    if (position < 3) {
      showFirstPageLayout()
    } else if (position == 3) {
      showLastPageLayout()
    }
  }

  private fun showLastPageLayout() {
    if (skipButton!!.visibility == View.VISIBLE) {
      animateHideButton(skipButton!!)
      skipButton!!.visibility = View.GONE
    }

    if (checkBox!!.isChecked) {
      if (nextButton!!.visibility == View.GONE) {
        animateShowButton(nextButton!!)
        nextButton!!.visibility = View.VISIBLE
        animateShowButton(redeemBonus!!)
        redeemBonus!!.visibility = View.VISIBLE
        animateCheckboxUp(termsConditionsLayout!!)
        termsConditionsLayout!!.visibility = View.VISIBLE
      }
      if (warningText!!.visibility == View.VISIBLE) {
        animateHideWarning(warningText!!)
        warningText!!.visibility = View.GONE
      }

      if (termsConditionsLayout!!.visibility == View.GONE) {
        termsConditionsLayout!!.visibility = View.VISIBLE
        animateCheckboxUp(termsConditionsLayout!!)
      }
    } else {
      if (warningText!!.visibility == View.GONE) {
        animateShowWarning(warningText!!)
        warningText!!.visibility = View.VISIBLE
      }
      if (nextButton!!.visibility == View.VISIBLE) {
        animateHideButton(nextButton!!)
        nextButton!!.visibility = View.GONE
        animateHideButton(redeemBonus!!)
        redeemBonus!!.visibility = View.GONE
      }

      if (termsConditionsLayout!!.visibility == View.GONE) {
        termsConditionsLayout!!.visibility = View.VISIBLE
        animateCheckboxDown(termsConditionsLayout!!)
      }
    }
  }

  private fun showFirstPageLayout() {
    if (skipButton!!.visibility == View.GONE) {
      animateShowButton(skipButton!!)
      skipButton!!.visibility = View.VISIBLE
    }

    if (nextButton!!.visibility == View.VISIBLE) {
      animateHideButton(nextButton!!)
      nextButton!!.visibility = View.GONE
      animateHideButton(redeemBonus!!)
      redeemBonus!!.visibility = View.GONE
    }

    if (termsConditionsLayout!!.visibility == View.VISIBLE) {
      animateCheckboxDown(termsConditionsLayout!!)
      termsConditionsLayout!!.visibility = View.GONE
    }

    if (warningText!!.visibility == View.VISIBLE) {
      animateHideWarning(warningText!!)
      warningText!!.visibility = View.GONE
    }
  }

  private fun updatePageIndicator(position: Int) {
    val pos: Int
    val config = view.resources.configuration
    pos = if (config.layoutDirection == View.LAYOUT_DIRECTION_LTR) {
      position
    } else {
      pageCount - position - 1
    }
    pageIndicatorView!!.setSelected(pos)
  }

  override fun onPageSelected(position: Int) {

  }

  override fun onPageScrollStateChanged(state: Int) {

  }
}