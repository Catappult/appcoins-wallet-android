package com.asfoundation.wallet.subscriptions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.subscription_item.view.*
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(item: SubscriptionItem, clickCallback: PublishSubject<String>?) {
    itemView.apply {
      app_name.text = item.appName

      if (item.expiresOn == null) {
        expires_on.visibility = View.GONE
        recurrence_value.visibility = View.VISIBLE

        recurrence_value.text = String.format("%s / %s",
            item.symbol + item.amount.setScale(FIAT_SCALE, RoundingMode.FLOOR), item.periodicity)
      } else {
        recurrence_value.visibility = View.GONE
        expires_on.visibility = View.VISIBLE

        val dateFormat = SimpleDateFormat("MMM yy", Locale.getDefault())

        expires_on.text = context.getString(R.string.subscriptions_expiration_body,
            dateFormat.format(item.expiresOn))
      }

      more_button.setOnClickListener { clickCallback?.onNext(item.packageName) }
      item_parent.setOnClickListener { clickCallback?.onNext(item.packageName) }
    }

    GlideApp.with(itemView.context)
        .load(item.iconUrl)
        .error(R.drawable.ic_transaction_peer)
        .into(itemView.app_icon)
  }

  companion object {
    private const val FIAT_SCALE = 2
  }

}