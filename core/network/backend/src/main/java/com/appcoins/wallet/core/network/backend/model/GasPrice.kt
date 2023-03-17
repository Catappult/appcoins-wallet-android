package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class GasPrice(@SerializedName("gas_price") val price: BigInteger)