package com.asfoundation.wallet.promotions.voucher

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import com.asf.wallet.R
import rx.subjects.PublishSubject


class SkuButtonsAdapter(
    val context: Context,
    val buttonModels: List<SkuButtonModel>,
    val onSkuClick: PublishSubject<Any>
) :
    BaseAdapter() {

  private var inflater: LayoutInflater
  private var activatedButton: Button? = null
  var selectedPosition = -1

  override fun getCount(): Int {
    return buttonModels.size
  }

  override fun getItem(i: Int): Any? {
    return null
  }

  override fun getItemId(i: Int): Long {
    return 0
  }

  override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
    val button: Button =
        inflater.inflate(R.layout.e_voucher_details_diamonds_button, null) as Button
    val factor: Float =
        view?.getContext()
            ?.getResources()
            ?.getDisplayMetrics()?.density ?: 0.0.toFloat()
    button.layoutParams = AbsListView.LayoutParams(GridView.AUTO_FIT, (factor * 48).toInt())

    button.setOnClickListener {
      if (activatedButton == null) {
        activatedButton = button
      } else {
        activatedButton?.setActivated(false)
        activatedButton = button
      }

      selectedPosition = i
      onSkuClick.onNext(0)
      button.setActivated(true)
    }
    button.text = buttonModels.get(i).title
    return button
  }

  init {
    inflater = LayoutInflater.from(context)
  }

  interface OnClick {
    fun onClick()
  }
}