package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.model

import android.util.Log
import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.asf.wallet.R
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.PaymentMethodClick
import com.asfoundation.wallet.ui.common.BaseViewHolder
import com.asfoundation.wallet.ui.common.WalletButtonView
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
    if (otherPaymentMethods.isNotEmpty()) {
      val methodsString = StringBuilder()
      for (paymentMethod in otherPaymentMethods) {
        Log.d(
          "APPC-CCC",
          "IncompletePaymentMethodsModel: handleMethodsList: method ${paymentMethod.label} "
        )
        methodsString.append(paymentMethod.label).append(", ")
      }
      holder.methodsTextList.visibility = View.VISIBLE
      holder.methodsTextList.text = methodsString
    }
  }

  override fun getDefaultLayout(): Int = R.layout.onboarding_incomplete_payment_methods_layout

  class OtherPaymentMethodLayoutHolder : BaseViewHolder() {
    val backToGameButton by bind<WalletButtonView>(R.id.back_to_game_button)
    val methodsTextList by bind<TextView>(R.id.incomplete_payment_methods_text_list)
  }
}