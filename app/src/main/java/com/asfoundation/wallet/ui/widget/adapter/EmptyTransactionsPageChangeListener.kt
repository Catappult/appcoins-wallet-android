package com.asfoundation.wallet.ui.widget.adapter

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R


class EmptyTransactionsPageChangeListener(view: View) : ViewPager.OnPageChangeListener {

  private var lottieView: LottieAnimationView =
      view.findViewById(R.id.transactions_empty_screen_animation)
  private var isSettled: Boolean = false


  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    if (positionOffset.compareTo(0.0) != 0) {
      lottieView.progress = 0f
      lottieView.cancelAnimation()
    }
  }

  override fun onPageScrollStateChanged(state: Int) {
    if (state == ViewPager.SCROLL_STATE_SETTLING) {
      isSettled = true
    }
    if (state == ViewPager.SCROLL_STATE_IDLE && isSettled) {
      lottieView.playAnimation()
      isSettled = false
    }
  }

  override fun onPageSelected(position: Int) {
  }
}