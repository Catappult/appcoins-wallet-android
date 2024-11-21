package com.asfoundation.wallet.firebase_messaging.repository.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TokenResponse(
  val token: String,
  val wallet: String,
  @SerializedName("ts") val date: String,
)
