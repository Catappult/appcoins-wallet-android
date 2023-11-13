package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName

data class CachedBackupResponse(
  @SerializedName("private_key") val backup: String?,
)