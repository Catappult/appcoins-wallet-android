package com.asfoundation.wallet.ui.widget.holder

import android.os.Bundle
import android.text.format.DateFormat
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_transactions_date_head.view.*
import java.util.*

class TransactionDateHolder(resId: Int, parent: ViewGroup) :
    BinderViewHolder<Date?>(resId, parent) {

  companion object {
    const val VIEW_TYPE = 1004
    private const val DATE_TEMPLATE = "MMM, dd yyyy"
  }

  override fun bind(data: Date?, addition: Bundle) {
    if (data == null) {
      itemView.title.text = null
    } else {
      val calendar = Calendar.getInstance()
      calendar.timeZone = TimeZone.getTimeZone("UTC")
      calendar.time = data
      itemView.title.text = DateFormat.format(DATE_TEMPLATE,
          calendar)
    }
  }

}