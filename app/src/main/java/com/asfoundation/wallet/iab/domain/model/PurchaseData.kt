package com.asfoundation.wallet.iab.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import kotlin.random.Random

@Parcelize
data class PurchaseData(
  val referrerUrl: String,
  val type: String? = null, // this one is strange. we have a TYPE but we use the ESKILLS to build this in OneStepTransactionParser
  val origin: String? = null,
  val skuId: String? = null,
  val domain: String,
  val productToken: String? = null,
  val payload: String? = null,
  val callbackUrl: String? = null,
  val orderReference: String? = null,
  val signature: String? = null,
  val purchaseValue: String? = null, // originalOneStepValue in previous implementation
  val currency: String? = null, // originalOneStepCurrency in previous implementation
  val oemId: String? = null,
  val oemPackage: String? = null,
  val erc681Amount: BigDecimal? = null,
  val symbol: String? = null,
) : Parcelable

val emptyPurchaseData = PurchaseData(
  skuId = "PurchaseRequest product",
  type = "Type",
  origin = "Origin",
  domain = "PurchaseRequest domain",
  callbackUrl = "PurchaseRequest callback url",
  orderReference = "PurchaseRequest order reference",
  purchaseValue = Random.nextDouble(1.0, 100.0).toString(),
  currency = "PurchaseRequest currency",
  oemId = "PurchaseRequest oemid",
  oemPackage = "PurchaseRequest oempackage",
  referrerUrl = Uri.EMPTY.toString(),
  payload = "metadata",
  productToken = "Product Token",
  signature = "",
)
