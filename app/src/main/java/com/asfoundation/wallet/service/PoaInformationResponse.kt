package com.asfoundation.wallet.service

import com.google.gson.annotations.SerializedName

data class PoaInformationResponse(@SerializedName("hours") val hoursRemaining: Int,
                                  @SerializedName("remaining_poa") val remainingPoa: Int,
                                  @SerializedName("minutes") val minutesRemaining: Int)