package com.asfoundation.wallet.ui.onboarding

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R
import com.rd.PageIndicatorView


class OnboardingPageChangeListener internal constructor(private val view: View,
                                                        private var paymentMethodsIcons: List<String> = emptyList()) :
    ViewPager2.OnPageChangeCallback() {

  companion object {
    private const val ANIMATION_TRANSITIONS = 3
    private const val PAGE_COUNT = 3
  }

  private var lottieViewPortrait: LottieAnimationView? = null
  private var lottieViewLandscape: LottieAnimationView? = null
  private lateinit var skipButton: Button
  private lateinit var nextButton: Button
  private lateinit var paymentMethodsRecyclerView: RecyclerView
  private lateinit var checkBox: CheckBox
  private lateinit var warningText: TextView
  private lateinit var termsConditionsLayout: LinearLayout
  private lateinit var pageIndicatorView: PageIndicatorView
  private var currentPage = 0

  init {
    init()
  }

  fun init() {
    lottieViewPortrait = view.findViewById(R.id.lottie_onboarding_portrait)
    lottieViewLandscape = view.findViewById(R.id.lottie_onboarding_landscape)
    skipButton = view.findViewById(R.id.skip_button)
    nextButton = view.findViewById(R.id.next_button)
    checkBox = view.findViewById(R.id.onboarding_checkbox)
    paymentMethodsRecyclerView = view.findViewById(R.id.payment_methods_recycler_view)
    warningText = view.findViewById(R.id.terms_conditions_warning)
    termsConditionsLayout = view.findViewById(R.id.terms_conditions_layout)
    pageIndicatorView = view.findViewById(R.id.page_indicator)
    updatePageIndicator(0)
    handleUI(0)
  }

  fun setPaymentMethodsIcons(paymentMethodsIcons: List<String>) {
    this.paymentMethodsIcons = paymentMethodsIcons
    paymentMethodsRecyclerView.adapter = OnboardingPaymentMethodAdapter(paymentMethodsIcons)
    if (currentPage == 2) paymentMethodsRecyclerView.visibility = View.VISIBLE
  }

  private fun animateHideWarning(textView: TextView) {
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.fast_fade_out_animation)
    textView.animation = animation
  }

  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    lottieViewPortrait?.progress =
        position * (1f / ANIMATION_TRANSITIONS) + positionOffset * (1f / ANIMATION_TRANSITIONS)
    lottieViewLandscape?.progress =
        position * (1f / ANIMATION_TRANSITIONS) + positionOffset * (1f / ANIMATION_TRANSITIONS)
    checkBox.setOnClickListener { handleUI(position) }
    updatePageIndicator(position)
    currentPage = position
    handleUI(position)
  }

  private fun handleUI(position: Int) {
    if (position < 2) {
      showFirstPageLayout()
    } else if (position == 2) {
      showLastPageLayout()
      if (paymentMethodsIcons.isNotEmpty()) paymentMethodsRecyclerView.visibility = View.VISIBLE
    }
  }

  private fun showLastPageLayout() {
    skipButton.visibility = View.GONE
    nextButton.visibility = View.VISIBLE
    termsConditionsLayout.visibility = View.VISIBLE
    nextButton.isEnabled = checkBox.isChecked
    paymentMethodsRecyclerView.visibility = View.INVISIBLE

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
    termsConditionsLayout.visibility = View.GONE
    paymentMethodsRecyclerView.visibility = View.INVISIBLE

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
      PAGE_COUNT - position - 1
    }
    pageIndicatorView.setSelected(pos)
  }

  override fun onPageSelected(position: Int) = Unit

  override fun onPageScrollStateChanged(state: Int) = Unit
}