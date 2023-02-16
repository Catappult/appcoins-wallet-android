package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.model

import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.PaymentMethodClick
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.iab.PaymentMethod

@EpoxyModelClass
abstract class IncompletePaymentMethodsModel :
  EpoxyModelWithHolder<IncompletePaymentMethodsModel.OtherPaymentMethodLayoutHolder>() {

  @EpoxyAttribute
  lateinit var otherPaymentMethods: List<PaymentMethod>

  @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
  var clickListener: ((PaymentMethodClick) -> Unit)? = null

  override fun bind(holder: OtherPaymentMethodLayoutHolder) {
    holder.backToGameButton.setOnClickListener {
      clickListener?.invoke(PaymentMethodClick.OtherPaymentMethods)
    }
    handleMethodsList(holder, otherPaymentMethods)
  }

  private fun handleMethodsList(
    holder: OtherPaymentMethodLayoutHolder,
    otherPaymentMethods: List<PaymentMethod>
  ) {
    val methodsString = StringBuilder()
    for (paymentMethod in otherPaymentMethods) {
      methodsString.append(paymentMethod.label).append(", ")
    }
    methodsString.setLength((methodsString.length - 2).coerceAtLeast(0));
    holder.methodsTextList.text = methodsString
  }

  override fun getDefaultLayout(): Int = R.layout.onboarding_incomplete_payment_methods_layout

  class OtherPaymentMethodLayoutHolder : BaseViewHolder() {
    val backToGameButton by bind<ImageView>(R.id.back_to_game_arrow)
    val methodsTextList by bind<TextView>(R.id.incomplete_payment_methods_text_list)
    val otherMethodsContainer by bind<ConstraintLayout>(R.id.incomplete_payment_methods_layout)
  }
}