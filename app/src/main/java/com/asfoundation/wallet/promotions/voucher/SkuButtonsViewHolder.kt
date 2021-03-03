package com.asfoundation.wallet.promotions.voucher

import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject

class SkuButtonsViewHolder(private val button: Button) : RecyclerView.ViewHolder(button) {

  fun bind(position: Int, selectedPosition: Int, skuButtonModel: SkuButtonModel,
           onSkuClick: PublishSubject<Int>) {
    button.text = skuButtonModel.title

    button.isActivated = selectedPosition == position

    button.setOnClickListener { onSkuClick.onNext(position) }
  }
}