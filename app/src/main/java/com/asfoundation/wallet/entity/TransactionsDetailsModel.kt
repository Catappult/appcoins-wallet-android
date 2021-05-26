package com.asfoundation.wallet.entity

data class TransactionsDetailsModel(val networkInfo: NetworkInfo, val wallet: Wallet,
                                    val convertedValue: String) {
}