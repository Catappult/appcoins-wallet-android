package com.asfoundation.wallet.ui.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val itemMargin: Int) : RecyclerView.ItemDecoration() {
  override fun getItemOffsets(outRect: Rect, view: View,
                              parent: RecyclerView, state: RecyclerView.State) {
    with(outRect) {
      bottom = itemMargin
    }
  }
}