package com.asfoundation.wallet.ui.iab

import java.io.Serializable

class FiatValue(val amount: Double, val currency: String, val symbol: String = "") : Serializable
