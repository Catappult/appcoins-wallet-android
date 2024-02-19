package com.asfoundation.wallet.billing.googlepay.models

enum class GooglePayResult(val key: String) {
  SUCCESS("SUCCESS"),
  CANCEL("CANCEL"),
  ERROR("ERROR"),
}
