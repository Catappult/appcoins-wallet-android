package com.asfoundation.wallet.subscriptions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.subscription_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionViewHolder(itemView: View, private val currencyFormatUtils: CurrencyFormatUtils) :
    RecyclerView.ViewHolder(itemView) {

  fun bind(item: SubscriptionItem, clickCallback: PublishSubject<Pair<SubscriptionItem, View>>?) {
    itemView.apply {
      app_name.text = item.appName

      if ((item.status == Status.CANCELED || item.status == Status.PAUSED) && item.expire != null) {
        showToExpireInfo(this, item)
      } else {
        showPriceInfo(this, item)
      }
      more_button.setOnClickListener { clickCallback?.onNext(Pair(item, app_icon)) }
      item_parent.setOnClickListener { clickCallback?.onNext(Pair(item, app_icon)) }
    }

    GlideApp.with(itemView.context)
        .load(item.appIcon)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .error(R.drawable.ic_transaction_peer)
        .into(itemView.app_icon)
  }

  private fun showPriceInfo(view: View, item: SubscriptionItem) {
    val formattedAmount = currencyFormatUtils.formatCurrency(item.fiatAmount)
    view.expires_on.visibility = View.GONE
    view.recurrence_value.visibility = View.VISIBLE

    view.recurrence_value.text =
        item.period?.mapToSubsFrequency(view.context,
            view.context.getString(R.string.value_fiat, formattedAmount, item.fiatSymbol))
  }

  private fun showToExpireInfo(view: View, item: SubscriptionItem) {
    view.recurrence_value.visibility = View.GONE
    view.expires_on.visibility = View.VISIBLE

    val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())
    view.expires_on.text = view.context.getString(R.string.subscriptions_expiration_body,
        dateFormat.format(item.expire!!))
  }
}