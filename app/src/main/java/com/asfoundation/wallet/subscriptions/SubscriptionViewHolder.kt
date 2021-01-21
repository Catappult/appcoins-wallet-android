package com.asfoundation.wallet.subscriptions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.subscription_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionViewHolder(itemView: View, private val currencyFormatUtils: CurrencyFormatUtils) :
    RecyclerView.ViewHolder(itemView) {

  fun bind(item: SubscriptionItem, clickCallback: PublishSubject<SubscriptionItem>?) {
    val formattedAmount = currencyFormatUtils.formatCurrency(item.fiatAmount)
    itemView.apply {
      app_name.text = item.appName

      if (item.expire == null) {
        expires_on.visibility = View.GONE
        recurrence_value.visibility = View.VISIBLE

        recurrence_value.text =
            item.period?.mapToSubFrequency(context,
                context.getString(R.string.value_fiat, formattedAmount, item.fiatSymbol))
      } else {
        recurrence_value.visibility = View.GONE
        expires_on.visibility = View.VISIBLE

        val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())

        expires_on.text = context.getString(R.string.subscriptions_expiration_body,
            dateFormat.format(item.expire))
      }

      more_button.setOnClickListener { clickCallback?.onNext(item) }
      item_parent.setOnClickListener { clickCallback?.onNext(item) }
    }

    GlideApp.with(itemView.context)
        .load(item.appIcon)
        .error(R.drawable.ic_transaction_peer)
        .into(itemView.app_icon)
  }
}