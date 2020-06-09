package com.asfoundation.wallet.backup

data class NotificationNeeded(
    val isNeeded: Boolean,
    val walletAddress: String
)