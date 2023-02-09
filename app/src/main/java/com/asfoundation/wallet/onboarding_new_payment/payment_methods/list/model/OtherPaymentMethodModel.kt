package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.model

import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@EpoxyModelClass
abstract class OtherPaymentMethodModel :
  EpoxyModelWithHolder<OtherPaymentMethodModel.OtherPaymentMethodHolder>() {

  @EpoxyAttribute
  lateinit var paymentMethod: PaymentMethod

  override fun bind(holder: OtherPaymentMethodHolder) {
    GlideApp
      .with(holder.itemView.context)
      .load(Uri.parse(paymentMethod.iconUrl))
      .transition(DrawableTransitionOptions.withCrossFade())
      .into(holder.methodIcon)

    holder.methodName.text = paymentMethod.label
  }

  override fun getDefaultLayout(): Int = R.layout.onboarding_payment_other_option_item

  class OtherPaymentMethodHolder : BaseViewHolder() {
    val methodIcon by bind<ImageView>(R.id.payment_option_item_icon)
    val methodName by bind<TextView>(R.id.payment_option_item_title)
  }
}