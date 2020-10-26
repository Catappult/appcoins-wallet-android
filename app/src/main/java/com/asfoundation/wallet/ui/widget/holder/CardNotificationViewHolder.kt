package com.asfoundation.wallet.ui.widget.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.interact.UpdateNotification
import com.asfoundation.wallet.promotions.PromotionNotification
import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.referrals.ReferralNotification
import kotlinx.android.synthetic.main.item_card_notification.view.*
import kotlinx.android.synthetic.main.referral_notification_card.view.notification_dismiss_button
import rx.functions.Action2

class CardNotificationViewHolder(
    itemView: View,
    private val action: Action2<CardNotification, CardNotificationAction>
) : RecyclerView.ViewHolder(itemView) {

  fun bind(cardNotification: CardNotification) {
    setTitle(cardNotification)
    setBody(cardNotification)
    setImage(cardNotification)
    setButtonActions(cardNotification)
  }

  private fun setTitle(cardNotification: CardNotification) {
    when (cardNotification) {
      is ReferralNotification -> itemView.notification_title.text =
          itemView.context.getString(cardNotification.title,
              cardNotification.symbol + cardNotification.pendingAmount)
      is PromotionNotification -> itemView.notification_title.text = cardNotification.noResTitle
      else -> cardNotification.title?.let {
        itemView.notification_title.text = itemView.context.getString(it)
      }
    }
  }

  private fun setBody(cardNotification: CardNotification) {
    if (cardNotification is PromotionNotification) {
      itemView.notification_description.text = cardNotification.noResBody
    } else {
      cardNotification.body?.let { itemView.notification_description.setText(it) }
    }
  }

  private fun setImage(cardNotification: CardNotification) {
    when (cardNotification) {
      is UpdateNotification -> {
        itemView.notification_animation.setAnimation(cardNotification.animation)
        itemView.notification_image.visibility = View.INVISIBLE
        itemView.notification_animation.visibility = View.VISIBLE
      }
      is PromotionNotification -> {
        itemView.notification_image.visibility = View.VISIBLE
        itemView.notification_animation.visibility = View.INVISIBLE
        GlideApp.with(itemView.context)
            .load(cardNotification.noResIcon)
            .error(R.drawable.ic_promotions_default)
            .circleCrop()
            .into(itemView.notification_image)
      }
      else -> {
        itemView.notification_image.visibility = View.VISIBLE
        itemView.notification_animation.visibility = View.INVISIBLE
        cardNotification.icon?.let {
          itemView.notification_image.setImageResource(it)
          itemView.notification_image.visibility = View.VISIBLE
        }
      }
    }
  }

  private fun setButtonActions(cardNotification: CardNotification) {
    if (cardNotification is PromotionNotification) {
      itemView.notification_dismiss_button.setOnClickListener {
        action.call(cardNotification, CardNotificationAction.DISMISS)
      }
      itemView.setOnClickListener { action.call(cardNotification, cardNotification.positiveAction) }
      itemView.notification_positive_button.visibility = View.GONE
    } else {
      cardNotification.positiveButtonText?.let { itemView.notification_positive_button.setText(it) }
      itemView.notification_dismiss_button.setOnClickListener {
        action.call(cardNotification, CardNotificationAction.DISMISS)
      }
      itemView.notification_positive_button.setOnClickListener {
        action.call(cardNotification, cardNotification.positiveAction)
      }
    }
  }
}