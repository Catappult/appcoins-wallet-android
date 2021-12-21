package com.asfoundation.wallet.wallets.db.entity

import java.math.BigInteger

data class WalletInfoUpdate(
    val wallet: String,
    val appcCreditsBalanceWei: BigInteger,
    val appcBalanceWei: BigInteger,
    val ethBalanceWei: BigInteger,
    val blocked: Boolean,
    val verified: Boolean,
    val logging: Boolean)