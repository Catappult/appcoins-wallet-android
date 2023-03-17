package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.model

import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.PaymentMethodClick
import com.appcoins.wallet.ui.widgets.BaseViewHolder
import com.asfoundation.wallet.ui.iab.PaymentMethod
import com.asfoundation.wallet.ui.iab.PaymentMethodsMapper
import com.asfoundation.wallet.ui.iab.PaymentMethodsView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

@EpoxyModelClass
abstract class PaymentMethodModel : EpoxyModelWithHolder<PaymentMethodModel.PaymentMethodHolder>() {

  @EpoxyAttribute
  lateinit var paymentMethod: PaymentMethod

  @EpoxyAttribute
  lateinit var paymentMethodMapper: PaymentMethodsMapper

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((PaymentMethodClick) -> Unit)? = null

  override fun bind(holder: PaymentMethodHolder) {
    GlideApp
      .with(holder.itemView.context)
      .load(Uri.parse(paymentMethod.iconUrl))
      .transition(DrawableTransitionOptions.withCrossFade())
      .into(holder.methodIcon)

    holder.methodName.text = paymentMethod.label

    holder.methodContainer.setOnClickListener {
      handleMethodClick()
    }
  }

  private fun handleMethodClick() {
    //TODO change the mapper to decouple from the PaymentMethodsView, and remove unnecessary methods
    when (paymentMethodMapper.map(paymentId = paymentMethod.id)) {
      PaymentMethodsView.SelectedPaymentMethod.PAYPAL -> {
        clickListener?.invoke(PaymentMethodClick.PaypalAdyenClick)
      }
      PaymentMethodsView.SelectedPaymentMethod.PAYPAL_V2 -> {
        clickListener?.invoke(PaymentMethodClick.PaypalDirectClick)
      }
      PaymentMethodsView.SelectedPaymentMethod.CREDIT_CARD -> {
        clickListener?.invoke(PaymentMethodClick.CreditCardClick)
      }
      PaymentMethodsView.SelectedPaymentMethod.SHARE_LINK -> {
        clickListener?.invoke(PaymentMethodClick.ShareLinkPaymentClick)
      }
      PaymentMethodsView.SelectedPaymentMethod.LOCAL_PAYMENTS -> {
        clickListener?.invoke(PaymentMethodClick.LocalPaymentClick)
      }
      PaymentMethodsView.SelectedPaymentMethod.CARRIER_BILLING -> {
        clickListener?.invoke(PaymentMethodClick.CarrierBillingClick)
      }
      PaymentMethodsView.SelectedPaymentMethod.APPC -> Unit
      PaymentMethodsView.SelectedPaymentMethod.APPC_CREDITS -> Unit
      PaymentMethodsView.SelectedPaymentMethod.MERGED_APPC -> Unit
      PaymentMethodsView.SelectedPaymentMethod.EARN_APPC -> Unit
      PaymentMethodsView.SelectedPaymentMethod.ERROR -> Unit
    }
  }

  override fun getDefaultLayout(): Int = R.layout.onboarding_payment_option_item

  class PaymentMethodHolder : BaseViewHolder() {
    val methodIcon by bind<ImageView>(R.id.payment_option_item_icon)
    val methodName by bind<TextView>(R.id.payment_option_item_title)
    val methodContainer by bind<ConstraintLayout>(R.id.payment_option_item_layout)
  }
}