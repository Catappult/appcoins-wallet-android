package com.asfoundation.wallet.billing.amazonPay.models

enum class AmazonPayResult(val key: String) {
  SUCCESS("SUCCESS"),
  CANCEL("CANCEL"),
  ERROR("ERROR"),
}
