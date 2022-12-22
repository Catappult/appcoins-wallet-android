package com.asfoundation.wallet.onboarding.pending_payment.payment_methods.list.model

import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@EpoxyModelClass
abstract class PaymentMethodModel : EpoxyModelWithHolder<PaymentMethodModel.PaymentMethodHolder>() {

  @EpoxyAttribute
  lateinit var paymentMethod: PaymentMethod

  @EpoxyAttribute
  var selected: Boolean = false

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((PaymentMethod) -> Unit)? = null

  override fun bind(holder: PaymentMethodHolder) {
    GlideApp
      .with(holder.itemView.context)
      .load(Uri.parse(paymentMethod.iconUrl))
      .transition(DrawableTransitionOptions.withCrossFade())
      .into(holder.methodIcon)

    holder.methodName.text = paymentMethod.label

    holder.methodContainer.setOnClickListener { clickListener?.invoke(paymentMethod) }
  }

  override fun getDefaultLayout(): Int = R.layout.onboarding_payment_option_item

  class PaymentMethodHolder : BaseViewHolder() {
    val methodIcon by bind<ImageView>(R.id.payment_option_item_icon)
    val methodName by bind<TextView>(R.id.payment_option_item_title)
    val methodContainer by bind<ConstraintLayout>(R.id.payment_option_item_layout)
  }
}