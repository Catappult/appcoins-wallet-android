package com.asfoundation.wallet.ui.onboarding

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

  private lateinit var lottieView: LottieAnimationView
  private lateinit var skipButton: Button
  private lateinit var nextButton: Button
  private lateinit var redeemBonus: Button
  private lateinit var checkBox: CheckBox
  private lateinit var warningText: TextView
  private lateinit var termsConditionsLayout: LinearLayout
  private lateinit var pageIndicatorView: PageIndicatorView

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
    handleUI(0)
  }

  private fun animateHideWarning(textView: TextView) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.fast_fade_out_animation)
    textView.animation = animation
  }

  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    lottieView.progress =
        position * (1f / ANIMATION_TRANSITIONS) + positionOffset * (1f / ANIMATION_TRANSITIONS)
    checkBox.setOnClickListener { handleUI(position) }
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
    skipButton.visibility = View.GONE
    nextButton.visibility = View.VISIBLE
    redeemBonus.visibility = View.VISIBLE
    termsConditionsLayout.visibility = View.VISIBLE
    nextButton.isEnabled = checkBox.isChecked
    redeemBonus.isEnabled = checkBox.isChecked

    if (checkBox.isChecked) {
      if (warningText.visibility == View.VISIBLE) {
        animateHideWarning(warningText)
        warningText.visibility = View.INVISIBLE
      }
    }
  }

  private fun showFirstPageLayout() {
    skipButton.visibility = View.VISIBLE
    nextButton.visibility = View.GONE
    redeemBonus.visibility = View.GONE
    termsConditionsLayout.visibility = View.GONE

    if (warningText.visibility == View.VISIBLE) {
      animateHideWarning(warningText)
      warningText.visibility = View.INVISIBLE
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
    pageIndicatorView.setSelected(pos)
  }

  override fun onPageSelected(position: Int) {

  }

  override fun onPageScrollStateChanged(state: Int) {

  }
}