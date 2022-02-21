package com.asfoundation.wallet.service.currencies

import java.math.BigDecimal

data class ConversionResponseBody(val currency: String, val value: BigDecimal, val label: String, val sign: String)