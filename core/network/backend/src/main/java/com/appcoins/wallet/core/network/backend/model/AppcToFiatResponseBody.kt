package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AppcToFiatResponseBody(@SerializedName("Datetime") val datetime: String,
                                  @SerializedName("APPC") val appcValue: BigDecimal)