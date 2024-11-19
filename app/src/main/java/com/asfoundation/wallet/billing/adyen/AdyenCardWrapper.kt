package com.asfoundation.wallet.billing.adyen

import android.os.Parcelable
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdyenCardWrapper(
  val cardPaymentMethod: CardPaymentMethod,
  val shouldStoreCard: Boolean,
  val hasCvc: Boolean,
  val supportedShopperInteractions: List<String>
) : Parcelable
