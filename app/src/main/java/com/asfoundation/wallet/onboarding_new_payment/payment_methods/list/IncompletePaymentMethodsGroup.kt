package com.asfoundation.wallet.onboarding_new_payment.payment_methods.list

import android.animation.LayoutTransition
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder
import com.asf.wallet.R
import com.asfoundation.wallet.onboarding_new_payment.payment_methods.list.model.IncompletePaymentMethodsModel_
import com.asfoundation.wallet.ui.iab.PaymentMethod

class IncompletePaymentMethodsGroup(
  otherPaymentMethodList: List<PaymentMethod>,
  paymentMethodClickListener: ((PaymentMethodClick) -> Unit)? = null
) : EpoxyModelGroup(
  R.layout.item_incomplete_payment_method,
  buildModels(otherPaymentMethodList, paymentMethodClickListener)
) {

  private val layoutTransition = LayoutTransition()

  override fun onViewAttachedToWindow(holder: ModelGroupHolder) {
    super.onViewAttachedToWindow(holder)
    layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    holder.rootView.layoutTransition = layoutTransition
  }

  companion object {
    fun buildModels(
      otherPaymentMethodList: List<PaymentMethod>,
      paymentMethodClickListener: ((PaymentMethodClick) -> Unit)?
    ): List<EpoxyModel<*>> {
      val models = mutableListOf<EpoxyModel<*>>()

      models.add(
        IncompletePaymentMethodsModel_()
          .id("incomplete_payment_methods")
          .otherPaymentMethods(otherPaymentMethodList)
          .clickListener(paymentMethodClickListener)
      )
      return models
    }
  }
}