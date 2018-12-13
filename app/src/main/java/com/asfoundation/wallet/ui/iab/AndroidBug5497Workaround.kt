package com.asfoundation.wallet.ui.iab

import android.app.Activity
import android.graphics.Rect
import android.view.ViewGroup
import android.widget.FrameLayout

class AndroidBug5497Workaround constructor(val activity: Activity) {

  private val contentContainer = activity.findViewById(android.R.id.content) as ViewGroup
  private val rootView = contentContainer.getChildAt(0)
  private val listener = { possiblyResizeChildOfContent() }

  private val contentAreaOfWindowBounds = Rect()
  private var usableHeightPrevious = 0

  fun addListener() {
    rootView?.viewTreeObserver?.addOnGlobalLayoutListener(listener)
  }

  fun removeListener() {
    if (!activity.isFinishing) {
      rootView?.viewTreeObserver?.removeOnGlobalLayoutListener(listener)
    }
  }

  private fun possiblyResizeChildOfContent() {
    contentContainer.getWindowVisibleDisplayFrame(contentAreaOfWindowBounds)
    val usableHeightNow = contentAreaOfWindowBounds.height()
    if (rootView != null && !(usableHeightPrevious - usableHeightNow >= -1 && usableHeightPrevious - usableHeightNow <= 1)) {
      val rootViewLayout = rootView.layoutParams as FrameLayout.LayoutParams
      rootViewLayout.height = usableHeightNow
      // Change the bounds of the root view to prevent gap between keyboard and content, and top of content positioned above top screen edge.
      rootView.layout(contentAreaOfWindowBounds.left, contentAreaOfWindowBounds.top,
          contentAreaOfWindowBounds.right, contentAreaOfWindowBounds.bottom)
      rootView.requestLayout()

      usableHeightPrevious = usableHeightNow
    }
  }
}