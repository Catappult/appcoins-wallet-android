package com.asfoundation.wallet.promotions.voucher

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.GridView
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import io.reactivex.subjects.PublishSubject


class SkuButtonsAdapter(private val buttonModels: List<SkuButtonModel>,
                        private val onSkuClick: PublishSubject<Int>) :
    RecyclerView.Adapter<SkuButtonsViewHolder>() {

  private var selectedPosition: Int = -1


  override fun onBindViewHolder(holder: SkuButtonsViewHolder, position: Int) {
    holder.bind(position, selectedPosition, buttonModels[position], onSkuClick)
  }

  override fun getItemCount() = buttonModels.size

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkuButtonsViewHolder {
    val context = parent.context
    val buttonHeight = context.resources.getDimension(R.dimen.voucher_details_grid_button_height)
    val button: Button = LayoutInflater.from(context)
        .inflate(R.layout.voucher_details_diamonds_button, parent, false) as Button
    button.layoutParams = AbsListView.LayoutParams(GridView.AUTO_FIT, buttonHeight.toInt())

    return SkuButtonsViewHolder(button)
  }

  fun setSelectedSku(index: Int) {
    val oldSelectedPosition = selectedPosition
    selectedPosition = index
    notifyItemChanged(oldSelectedPosition)
    notifyItemChanged(selectedPosition)
  }

  fun getSelectedSku(): SkuButtonModel {
    return if (selectedPosition != -1 && selectedPosition < buttonModels.size) {
      buttonModels[selectedPosition]
    } else {
      SkuButtonModel()
    }
  }
}

class MarginItemDecoration(private val horizontalSize: Int, private val verticalSize: Int) :
    RecyclerView.ItemDecoration() {
  override fun getItemOffsets(
      outRect: Rect, view: View,
      parent: RecyclerView,
      state: RecyclerView.State
  ) {

    with(outRect) {
      top = verticalSize / 2
      left = horizontalSize / 2
      right = horizontalSize / 2
      bottom = verticalSize / 2
    }
  }
}