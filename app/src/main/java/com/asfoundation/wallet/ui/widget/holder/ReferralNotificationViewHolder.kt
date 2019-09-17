package com.asfoundation.wallet.ui.widget.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asfoundation.wallet.referrals.ReferralNotification
import kotlinx.android.synthetic.main.referral_notification_card.view.*
import rx.functions.Action2

class ReferralNotificationViewHolder(
    itemView: View,
    private val action: Action2<ReferralNotification, ReferralNotificationAction>
) : RecyclerView.ViewHolder(itemView) {

  fun bind(referralNotification: ReferralNotification) {
    itemView.notification_title.text =
        itemView.context.getString(referralNotification.title,
            referralNotification.symbol + referralNotification.pendingAmount)
    itemView.notification_description.setText(referralNotification.body)
    itemView.notification_image.setImageResource(referralNotification.icon)
    itemView.notification_dismiss_button.setOnClickListener {
      action.call(referralNotification, ReferralNotificationAction.DISMISS)
    }
    itemView.notification_apps_games_button.setOnClickListener {
      action.call(referralNotification, ReferralNotificationAction.DISCOVER)
    }
  }

}