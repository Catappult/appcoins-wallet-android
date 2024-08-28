package com.asfoundation.wallet.iab.domain.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import kotlin.random.Random

@Parcelize
data class PurchaseData(
  val referrerUrl: String,
  val type: String, // this one is strange. we have a TYPE but we use the ESKILLS to build this in OneStepTransactionParser
  val origin: String?,
  val skuId: String?,
  val domain: String,
  val productToken: String?,
  val payload: String?,
  val callbackUrl: String?,
  val orderReference: String?,
  val value: String?, // originalOneStepValue in previous implementation
  val currency: String?, // originalOneStepCurrency in previous implementation
  val oemId: String?,
  val oemPackage: String?,
) : Parcelable

val emptyPurchaseData = PurchaseData(
  skuId = "PurchaseRequest product",
  type = "Type",
  origin = "Origin",
  domain = "PurchaseRequest domain",
  callbackUrl = "PurchaseRequest callback url",
  orderReference = "PurchaseRequest order reference",
  value = Random.nextDouble(1.0, 100.0).toString(),
  currency = "PurchaseRequest currency",
  oemId = "PurchaseRequest oemid",
  oemPackage = "PurchaseRequest oempackage",
  referrerUrl = Uri.EMPTY.toString(),
  payload = "metadata",
  productToken = "Product Token",
)
