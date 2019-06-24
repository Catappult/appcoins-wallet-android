package com.asfoundation.wallet.ui.onboarding

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R


class OnboardingPageChangeListener internal constructor(private val view: View) :
    ViewPager.OnPageChangeListener {

  companion object {
    var ANIMATION_TRANSITIONS = 3
  }

  private var lottieView: LottieAnimationView? = null
  private var skipButton: Button? = null
  private var checkBox: CheckBox? = null
  private var warningText: TextView? = null
  private var termsConditionsLayout: LinearLayout? = null

  init {
    init()
  }

  fun init() {
    lottieView = view.findViewById(R.id.lottie_onboarding)
    skipButton = view.findViewById(R.id.skip_button)
    checkBox = view.findViewById(R.id.onboarding_checkbox)
    warningText = view.findViewById(R.id.terms_conditions_warning)
    termsConditionsLayout = view.findViewById(R.id.terms_conditions_layout)
  }

  private fun showWarningText(position: Int) {
    if (!checkBox!!.isChecked && position == 3) {
      animateShowWarning(warningText!!)
      warningText!!.visibility = View.VISIBLE
    } else {
      if (warningText!!.visibility == View.VISIBLE) {
        animateHideWarning(warningText!!)
        warningText!!.visibility = View.GONE
      }
    }
  }

  private fun showButton(position: Int) {
    if (checkBox!!.isChecked) {
      if (skipButton!!.visibility != View.VISIBLE) {
        animateShowButton(skipButton!!)
        animateCheckboxUp(termsConditionsLayout!!)
        skipButton!!.visibility = View.VISIBLE
      }
    } else {
      if (skipButton!!.visibility == View.VISIBLE) {
        animateHideButton(skipButton!!)
        animateCheckboxDown(termsConditionsLayout!!)
        skipButton!!.visibility = View.GONE
      }
    }
    setButtonLabel(position)
  }

  private fun setButtonLabel(position: Int) {
    if (checkBox!!.isChecked) {
      if (position == 3) {
        skipButton!!.setText(R.string.button_ok)
      } else {
        skipButton!!.setText(R.string.intro_skip_button)
      }
    }
  }

  private fun animateCheckboxUp(layout: LinearLayout) {
    (AnimatorInflater.loadAnimator(view.context, R.animator.minor_translate_up) as AnimatorSet).apply {
      setTarget(layout)
      start()
    }
  }

  private fun animateCheckboxDown(layout: LinearLayout) {
    (AnimatorInflater.loadAnimator(view.context, R.animator.minor_translate_down) as AnimatorSet).apply {
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
    checkBox!!.setOnClickListener {
      showWarningText(position)
      showButton(position)
    }
    showWarningText(position)
    showButton(position)
  }

  override fun onPageSelected(position: Int) {

  }

  override fun onPageScrollStateChanged(state: Int) {

  }
}