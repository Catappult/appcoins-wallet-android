package com.asfoundation.wallet.iab.payment_manager.domain

import com.adyen.checkout.core.model.ModelObject

data class TransactionData(
  val adyenPaymentMethod: ModelObject,
  val shouldStoreMethod: Boolean,
  val hasCvc: Boolean,
  val supportedShopperInteractions: List<String>,
  val returnUrl: String,
  val value: String,
  val currency: String,
  val reference: String? = null,
  val paymentType: String,
  val walletAddress: String,
  val origin: String? = null,
  val packageName: String?,
  val metadata: String? = null,
  val sku: String? = null,
  val callbackUrl: String? = null,
  val transactionType: String,
  val entityOemId: String? = null,
  val entityDomain: String? = null,
  val entityPromoCode: String? = null,
  val userWallet: String? = null,
  val walletSignature: String,
  val referrerUrl: String? = null,
  val guestWalletId: String? = null,
)