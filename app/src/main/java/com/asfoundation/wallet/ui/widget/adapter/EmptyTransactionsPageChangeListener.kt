package com.asfoundation.wallet.ui.widget.adapter

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R


class EmptyTransactionsPageChangeListener(view: View) : ViewPager.OnPageChangeListener {

  private var lottieView: LottieAnimationView = view.findViewById(R.id.transactions_empty_screen_animation)


  override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
  }

  override fun onPageScrollStateChanged(state: Int) {
    if (state == ViewPager.SCROLL_STATE_DRAGGING) {
      lottieView.progress = 0f
      lottieView.cancelAnimation()
    }
    if (state == ViewPager.SCROLL_STATE_IDLE) {
      lottieView.playAnimation()
    }
  }

  override fun onPageSelected(position: Int) {
  }
}