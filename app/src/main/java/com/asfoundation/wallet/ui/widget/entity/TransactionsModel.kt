package com.asfoundation.wallet.ui.widget.entity

import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.viewmodel.TransactionsWalletModel

data class TransactionsModel(
    val notifications: List<CardNotification>,
    val maxBonus: Double,
    val transactionsWalletModel: TransactionsWalletModel
)
