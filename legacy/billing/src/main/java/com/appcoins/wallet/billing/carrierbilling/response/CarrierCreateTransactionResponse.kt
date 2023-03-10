package com.appcoins.wallet.billing.carrierbilling.response

import com.appcoins.wallet.billing.common.response.TransactionFee
import com.appcoins.wallet.billing.common.response.TransactionStatus

data class CarrierCreateTransactionResponse(val uid: String,
                                            val status: TransactionStatus,
                                            val url: String,
                                            val fee: TransactionFee,
                                            val carrier: TransactionCarrier)