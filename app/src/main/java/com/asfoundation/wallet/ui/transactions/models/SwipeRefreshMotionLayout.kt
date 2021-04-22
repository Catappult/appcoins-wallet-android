package com.asfoundation.wallet.ui.transactions.models

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class SwipeRefreshMotionLayout : MotionLayout {

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr)

  override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
    if (!isInteractionEnabled) {
      return
    }

    if (target !is SwipeRefreshLayout) {
      return super.onNestedPreScroll(target, dx, dy, consumed, type)
    }

    val recyclerView = target.getChildAt(0)
    if (recyclerView !is RecyclerView) {
      return super.onNestedPreScroll(target, dx, dy, consumed, type)
    }

    val canScrollVertically = recyclerView.canScrollVertically(-1)
    if (dy < 0 && canScrollVertically) {
      // don't start motionLayout transition
      return;
    }

    super.onNestedPreScroll(target, dx, dy, consumed, type)
  }
}