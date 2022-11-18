package com.asfoundation.wallet.home.ui.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyRecyclerView
import com.asf.wallet.R

class HomeRecyclerView @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : EpoxyRecyclerView(context, attrs, defStyleAttr) {

  init {
    // This layout manager centers the selected items (Loading, Empty state)
    // if there's available space to do so
    this.setBackgroundColor(ContextCompat.getColor(context, R.color.styleguide_white))
    layoutManager = object : LinearLayoutManager(context) {

      override fun layoutDecoratedWithMargins(child: View, left: Int, top: Int, right: Int,
                                              bottom: Int) {
        val viewHolder = getChildViewHolder(child)
        if (!shouldCenterItem(viewHolder.itemViewType))
          return super.layoutDecoratedWithMargins(child, left, top, right, bottom)

        val childHeight = bottom - top
        val offset = ((height - top) / 2) - (childHeight / 2)
        val heightDifferenceBottom = height - child.bottom
        return if (heightDifferenceBottom > 5) { // > 5 to avoid unnecessary floating
          super.layoutDecoratedWithMargins(child, left, top + offset, right, bottom + offset)
        } else {
          super.layoutDecoratedWithMargins(child, left, top, right, bottom)
        }
      }

      private fun shouldCenterItem(itemViewType: Int): Boolean {
        return itemViewType == R.layout.item_loading ||
            itemViewType == R.layout.layout_empty_transactions
      }
    }
  }


}