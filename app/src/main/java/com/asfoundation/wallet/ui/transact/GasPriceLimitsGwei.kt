package com.asfoundation.wallet.ui.transact

import java.math.BigDecimal

/**
 * Has the limits of GasPrice and the gas price itself in Gwei
 */
data class GasPriceLimitsGwei(val price: BigDecimal, val min: BigDecimal,
                              val max: BigDecimal)