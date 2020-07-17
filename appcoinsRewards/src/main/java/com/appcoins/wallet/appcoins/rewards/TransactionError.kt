package com.appcoins.wallet.appcoins.rewards

data class TransactionError(val status: Transaction.Status, val errorCode: Int?,
                            val errorMessage: String?)