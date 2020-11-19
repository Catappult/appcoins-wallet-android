package com.asfoundation.wallet.ui.widget.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.ui.widget.holder.CardNotificationAction
import com.asfoundation.wallet.ui.widget.holder.CardNotificationViewHolder
import com.asfoundation.wallet.util.convertDpToPx
import rx.functions.Action2

class CardNotificationsAdapter(
    var notifications: List<CardNotification>,
    private val listener: Action2<CardNotification, CardNotificationAction>
) : RecyclerView.Adapter<CardNotificationViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup,
                                  viewType: Int): CardNotificationViewHolder {

    val item = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_card_notification, parent,
            false)

    val maxWith = 400.convertDpToPx(parent.context.resources)

    val margins = 32.convertDpToPx(parent.context.resources)

    if (itemCount > 1) {
      val screenWith =
          TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, parent.measuredWidth.toFloat(),
              parent.context.resources
                  .displayMetrics)
              .toInt()

      if (screenWith > maxWith) {
        val lp = item.layoutParams as ViewGroup.LayoutParams
        lp.width = maxWith
        item.layoutParams = lp
      } else {
        val lp = item.layoutParams as ViewGroup.LayoutParams
        lp.width = screenWith - margins
        item.layoutParams = lp
      }
    }

    return CardNotificationViewHolder(item, listener)
  }

  override fun getItemCount() = notifications.size

  override fun onBindViewHolder(holder: CardNotificationViewHolder, position: Int) {
    holder.bind(notifications[position])
  }

}