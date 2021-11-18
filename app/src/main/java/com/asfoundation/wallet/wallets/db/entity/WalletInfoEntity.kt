package com.asfoundation.wallet.wallets.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigInteger

@Entity
data class WalletInfoEntity(
    @PrimaryKey val wallet: String,
    val ethBalanceWei: BigInteger,
    val appcBalanceWei: BigInteger,
    val appcCreditsBalanceWei: BigInteger,
    val blocked: Boolean,
    val verified: Boolean,
    val logging: Boolean,
)