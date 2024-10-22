package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName


data class AmazonPayChargePermissionResponse(
  @SerializedName("uid") val uid: String?,
  @SerializedName("charge_permission_id") val chargePermissionId: String?,
)