package com.asfoundation.wallet.home.ui.list.transactions

import android.text.format.DateFormat
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.appcoins.wallet.ui.widgets.BaseViewHolder
import java.util.*

@EpoxyModelClass
abstract class DateModel : EpoxyModelWithHolder<DateModel.DateHolder>() {

  companion object {
    private const val DATE_TEMPLATE = "MMM, dd yyyy"
  }

  @EpoxyAttribute
  var date: Date? = null

  override fun bind(holder: DateHolder) {
    val actualDate = date
    if (actualDate == null) {
      holder.title.text = null
    } else {
      val calendar = Calendar.getInstance()
      calendar.timeZone = TimeZone.getTimeZone("UTC")
      calendar.time = actualDate
      holder.title.text = DateFormat.format(DATE_TEMPLATE, calendar)
    }
  }

  override fun getDefaultLayout(): Int = R.layout.item_transactions_date_head

  class DateHolder : BaseViewHolder() {
    val title by bind<TextView>(R.id.title)
  }

}