package com.asfoundation.wallet.wallet_validation

import java.io.Serializable

data class ValidationInfo(val code1: String, val code2: String, val code3: String,
                          val code4: String, val code5: String, val code6: String,
                          val countryCode: String, val phoneNumber: String) : Serializable