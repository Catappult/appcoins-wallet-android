package com.asfoundation.wallet.billing.adyen

import org.json.JSONObject

data class AdyenComponentResponseModel(val details: JSONObject?, val paymentData: String?)
