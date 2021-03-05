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


class VoucherSkuAdapter(private var voucherSkuList: List<VoucherSkuItem>,
                        private val onSkuClick: PublishSubject<Int>) :
    RecyclerView.Adapter<VoucherSkuViewHolder>() {

  private var selectedPosition: Int = -1

  override fun onBindViewHolder(holder: VoucherSkuViewHolder, position: Int) {
    holder.bind(selectedPosition, voucherSkuList[position], onSkuClick)
  }

  override fun onBindViewHolder(holder: VoucherSkuViewHolder, position: Int,
                                payloads: MutableList<Any>) {
    if (payloads.isEmpty()) onBindViewHolder(holder, position)
    else if (payloads.contains(SELECT_PAYLOAD)) holder.refreshState(selectedPosition)
  }

  override fun getItemCount() = voucherSkuList.size

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherSkuViewHolder {
    val context = parent.context
    val buttonHeight = context.resources.getDimension(R.dimen.voucher_details_grid_button_height)
    val button: Button = LayoutInflater.from(context)
        .inflate(R.layout.voucher_details_sku_button, parent, false) as Button
    button.layoutParams = AbsListView.LayoutParams(GridView.AUTO_FIT, buttonHeight.toInt())

    return VoucherSkuViewHolder(button)
  }

  fun setSelectedSku(index: Int) {
    val oldSelectedPosition = selectedPosition
    selectedPosition = index
    notifyItemChanged(oldSelectedPosition, SELECT_PAYLOAD)
    notifyItemChanged(selectedPosition, SELECT_PAYLOAD)
  }

  fun setSkus(voucherSkuItems: List<VoucherSkuItem>) {
    voucherSkuList = voucherSkuItems
    notifyDataSetChanged()
  }

  fun getSelectedPosition() = selectedPosition

  fun getSelectedSku(): VoucherSkuItem {
    return if (selectedPosition != -1 && selectedPosition < voucherSkuList.size) {
      voucherSkuList[selectedPosition]
    } else {
      VoucherSkuItem()
    }
  }

  private companion object {
    private const val SELECT_PAYLOAD = "select"
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