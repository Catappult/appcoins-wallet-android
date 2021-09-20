package com.asfoundation.wallet.ui.transactions.models

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Handles both swipe refresh and saves/restores state of transition
 */
class SwipeRefreshMotionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

  override fun onSaveInstanceState(): Parcelable {
    return SaveState(super.onSaveInstanceState(), startState, endState, targetPosition)
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    (state as? SaveState)?.let {
      super.onRestoreInstanceState(it.superParcel)
      setTransition(it.startState, it.endState)
      progress = it.progress
    }
  }

  @kotlinx.android.parcel.Parcelize
  private class SaveState(
      val superParcel: Parcelable?,
      val startState: Int,
      val endState: Int,
      val progress: Float
  ) : Parcelable

  override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
    if (!isInteractionEnabled) {
      return
    }

    if (target !is SwipeRefreshLayout) {
      return super.onNestedPreScroll(target, dx, dy, consumed, type)
    }

    val recyclerView = getRecyclerViewChild(target)
        ?: return super.onNestedPreScroll(target, dx, dy, consumed, type)

    val canScrollVertically = recyclerView.canScrollVertically(-1)
    if (dy < 0 && canScrollVertically) {
      // don't start motionLayout transition
      return;
    }

    super.onNestedPreScroll(target, dx, dy, consumed, type)
  }

  private fun getRecyclerViewChild(swipe: SwipeRefreshLayout): RecyclerView? {
    return swipe.children
        .filterIsInstance<RecyclerView>()
        .firstOrNull()
  }
}