package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class CreditCardCVCResponse(@SerializedName("ask_cvc") val needAskCvc: Boolean)
