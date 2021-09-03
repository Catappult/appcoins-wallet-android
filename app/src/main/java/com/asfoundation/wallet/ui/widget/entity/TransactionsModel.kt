package com.asfoundation.wallet.ui.widget.entity

import com.asfoundation.wallet.referrals.CardNotification
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.appcoins.applications.AppcoinsApplication

data class TransactionsModel(
    val transactions: List<Transaction>,
    val notifications: List<CardNotification>,
    val applications: List<AppcoinsApplication>
)