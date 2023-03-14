package com.appcoins.wallet.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ViewSwitcher
import com.asf.wallet.R

/**
 * Generic animated view switcher. Because of animation specifications, it really only supports
 * two views.
 */
class AnimatedViewSwitcher : ViewSwitcher {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  fun showFirstView() {
    if (displayedChild == 0) return
    setInAnimation(context, R.anim.slide_in_down)
    setOutAnimation(context, R.anim.slide_out_up)
    showNext()
  }

  fun showSecondView() {
    if (displayedChild == 1) return
    setInAnimation(context, R.anim.slide_in_down)
    setOutAnimation(context, R.anim.slide_out_up)
    showPrevious()
  }
}