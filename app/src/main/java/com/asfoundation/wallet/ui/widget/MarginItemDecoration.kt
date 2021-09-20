package com.asfoundation.wallet.ui.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asfoundation.wallet.util.convertDpToPx

class MarginItemDecoration(private val itemMargin: Int) : RecyclerView.ItemDecoration() {
  override fun getItemOffsets(outRect: Rect, view: View,
                              parent: RecyclerView, state: RecyclerView.State) {
    if (parent.getChildAdapterPosition(view) == 0) {
      with(outRect) {
        top = 16.convertDpToPx(view.context.resources)
        bottom = itemMargin
      }
    }
    with(outRect) {
      bottom = itemMargin
    }
  }
}