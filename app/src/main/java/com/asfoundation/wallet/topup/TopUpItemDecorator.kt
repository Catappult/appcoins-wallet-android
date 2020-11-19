package com.asfoundation.wallet.topup

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asfoundation.wallet.util.convertDpToPx


class TopUpItemDecorator(private val size: Int, private val addMargin: Boolean) :
    RecyclerView.ItemDecoration() {

  override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                              state: RecyclerView.State) {
    if (addMargin) {
      val position: Int = parent.getChildAdapterPosition(view)
      val spanCount = size

      val screenWidth = parent.measuredWidth.convertDpToPx(parent.context.resources)
      val viewWidth = 80.convertDpToPx(parent.context.resources)

      val spacing = (((screenWidth - viewWidth * spanCount) / (spanCount + 1)) * 0.99).toInt()

      when {
        position == 0 -> {
          outRect.left = spacing
          outRect.right = spacing / 2
        }
        position < (parent.adapter?.itemCount ?: 0) - 1 -> {
          outRect.left = spacing / 2
          outRect.right = spacing / 2
        }
        else -> {
          outRect.left = spacing / 2
          outRect.right = spacing
        }
      }
    }
  }

}