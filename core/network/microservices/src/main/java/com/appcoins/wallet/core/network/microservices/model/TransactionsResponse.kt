package com.appcoins.wallet.core.network.microservices.model

import com.appcoins.wallet.core.network.microservices.model.Transaction as TransactionCore

data class TransactionsResponse(val items: List<TransactionCore>)