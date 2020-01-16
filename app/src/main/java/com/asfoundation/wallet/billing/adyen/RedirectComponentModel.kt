package com.asfoundation.wallet.billing.adyen

import org.json.JSONObject

data class RedirectComponentModel(val uid: String, val details: JSONObject,
                                  val paymentData: String?)
