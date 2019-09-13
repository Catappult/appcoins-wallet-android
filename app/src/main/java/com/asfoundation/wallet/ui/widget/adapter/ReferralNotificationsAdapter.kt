package com.asfoundation.wallet.ui.widget.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.referrals.ReferralNotification
import com.asfoundation.wallet.ui.widget.holder.ReferralNotificationAction
import com.asfoundation.wallet.ui.widget.holder.ReferralNotificationViewHolder
import rx.functions.Action2

class ReferralNotificationsAdapter(
    var notifications: List<ReferralNotification>,
    private val listener: Action2<ReferralNotification, ReferralNotificationAction>
) : RecyclerView.Adapter<ReferralNotificationViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup,
                                  viewType: Int): ReferralNotificationViewHolder {

    val item = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_referral_notification, parent,
            false)

    val maxWith = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400.toFloat(),
        parent.context.resources
            .displayMetrics)
        .toInt()

    val margins = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32.toFloat(),
        parent.context.resources
            .displayMetrics)
        .toInt()

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

    return ReferralNotificationViewHolder(item, listener)
  }

  override fun getItemCount() = notifications.size

  override fun onBindViewHolder(holder: ReferralNotificationViewHolder, position: Int) {
    holder.bind(notifications[position])
  }

}