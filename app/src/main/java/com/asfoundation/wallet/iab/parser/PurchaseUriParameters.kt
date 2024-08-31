package com.asfoundation.wallet.iab.parser

import com.asf.wallet.BuildConfig
import com.appcoins.wallet.billing.BuildConfig as SDKBuildConfig

const val PURCHASE_URI_SDK_SCHEME = SDKBuildConfig.SDK_SCHEME
const val PURCHASE_URI_OSP_SCHEME = BuildConfig.OSP_SCHEME
const val PAYMENT_TYPE_INAPP_UNMANAGED = "INAPP_UNMANAGED"
const val PAYMENT_TYPE_INAPP = "INAPP"
const val BDS_ORIGIN = "BDS"
const val ETH = "ETH"

object PurchaseUriParameters {
  const val PRODUCT = "product"
  const val DOMAIN = "domain"
  const val CALLBACK_URL = "callback_url"
  const val ORDER_REFERENCE = "order_reference"
  const val SIGNATURE = "signature"
  const val VALUE = "value"
  const val CURRENCY = "currency"
  const val METADATA = "data"
  const val PRODUCT_TOKEN = "product_token"
  const val TYPE = "type"
  const val ORIGIN = "origin"
}
