package com.appcoins.wallet.core.network.microservices.model

data class CarrierCreateTransactionResponse(val uid: String,
                                            val status: TransactionStatus,
                                            val url: String,
                                            val fee: TransactionFee,
                                            val carrier: TransactionCarrier)