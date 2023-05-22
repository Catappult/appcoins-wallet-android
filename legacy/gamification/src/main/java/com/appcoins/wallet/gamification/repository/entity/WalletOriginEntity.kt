package com.appcoins.wallet.gamification.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appcoins.wallet.core.network.backend.model.WalletOrigin

@Entity
data class WalletOriginEntity(
    @PrimaryKey @ColumnInfo(name = "wallet_address") val walletAddress: String,
    @ColumnInfo(name = "wallet_origin") val walletOrigin: WalletOrigin)
