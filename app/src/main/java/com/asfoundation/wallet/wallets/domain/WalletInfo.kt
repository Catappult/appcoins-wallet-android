package com.asfoundation.wallet.wallets.domain

import java.math.BigInteger

data class WalletInfo(
    val wallet: String,
    val ethBalanceWei: BigInteger,
    val appcBalanceWei: BigInteger,
    val appcCreditsBalanceWei: BigInteger,
    val blocked: Boolean,
    val verified: Boolean,
    val logging: Boolean,
)