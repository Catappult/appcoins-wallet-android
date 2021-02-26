package com.asfoundation.wallet.promotions.voucher

import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicReference

class SkuButtonsViewHolder(private val button: Button) : RecyclerView.ViewHolder(button) {

  fun bind(position: Int, selectedPosition: Int, skuButtonModel: SkuButtonModel,
           activatedButton: AtomicReference<Button?>, onSkuClick: PublishSubject<Int>) {
    button.text = skuButtonModel.title

    if (selectedPosition == position) {
      button.isActivated = true
    } else {
      button.isActivated = false
    }

    button.setOnClickListener {
      if (activatedButton.get() == null) {
        activatedButton.set(button)
      } else {
        activatedButton.get()
            ?.setActivated(false)
        activatedButton.set(button)
      }

      onSkuClick.onNext(position)
    }
  }
}