package com.asfoundation.wallet.topup

import java.io.Serializable

data class LocalCurrency(val symbol: String = "", val code: String = "") : Serializable
