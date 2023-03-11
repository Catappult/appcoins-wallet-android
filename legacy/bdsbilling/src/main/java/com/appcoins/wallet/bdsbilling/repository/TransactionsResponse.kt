package com.appcoins.wallet.bdsbilling.repository

import com.appcoins.wallet.bdsbilling.repository.entity.Transaction

data class TransactionsResponse(val items: List<Transaction>)