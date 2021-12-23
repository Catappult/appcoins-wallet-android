package com.asfoundation.wallet.home.ui.list.transactions.empty

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.airbnb.lottie.LottieAnimationView
import com.asf.wallet.R
import com.asfoundation.wallet.home.ui.list.HomeListClick
import com.asfoundation.wallet.ui.common.BaseViewHolder

@EpoxyModelClass
abstract class EmptyItemModel :
    EpoxyModelWithHolder<EmptyItemModel.EmptyItemHolder>() {

  @EpoxyAttribute
  lateinit var emptyItem: EmptyItem

  @EpoxyAttribute
  var clickListener: ((HomeListClick) -> Unit)? = null

  override fun bind(holder: EmptyItemHolder) {
    super.bind(holder)
    holder.animation.setAnimation(emptyItem.animationRes)
    holder.emptyBodyText.text = emptyItem.bodyText

    if (emptyItem.actionText.isNotEmpty()) {
      holder.emptyActionText.text = emptyItem.actionText
      holder.emptyActionText.visibility = View.VISIBLE
    } else {
      holder.emptyActionText.visibility = View.GONE
    }
    holder.animation.setOnClickListener {
      clickListener?.invoke(HomeListClick.EmptyStateClick(emptyItem.id))
    }
    holder.emptyActionText.setOnClickListener {
      clickListener?.invoke(HomeListClick.EmptyStateClick(emptyItem.id))
    }
  }

  override fun getDefaultLayout(): Int = R.layout.layout_empty_transactions_viewpager

  class EmptyItemHolder : BaseViewHolder() {
    val animation by bind<LottieAnimationView>(R.id.transactions_empty_screen_animation)
    val emptyBodyText by bind<TextView>(R.id.empty_body_text)
    val emptyActionText by bind<TextView>(R.id.empty_action_text)
  }
}