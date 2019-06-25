package com.asfoundation.wallet.ui.widget.adapter

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R


class EmptyTransactionsPageChangeListener(view: View) : ViewPager.OnPageChangeListener {

  private var lottieView: LottieAnimationView =
      view.findViewById(R.id.transactions_empty_screen_animation)
  private var isAnimationPlaying: Boolean = false


  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    if (positionOffset.compareTo(0.0) != 0) {
      if (isAnimationPlaying) {
        lottieView.cancelAnimation()
        isAnimationPlaying = false
      }
    } else if (!isAnimationPlaying) {
      isAnimationPlaying = true
      lottieView.resumeAnimation()
    }
  }

  override fun onPageScrollStateChanged(state: Int) {
  }

  override fun onPageSelected(position: Int) {
  }
}