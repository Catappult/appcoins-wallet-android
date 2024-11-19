package com.asfoundation.wallet.iab.payment_manager.domain.payment_methods_transaction

import com.asfoundation.wallet.iab.payment_manager.domain.Transaction
import com.asfoundation.wallet.iab.payment_manager.domain.TransactionStatus
import kotlinx.coroutines.flow.Flow

class CreditCardTransaction(
  override val hash: String,
) : Transaction {

  override val status: Flow<TransactionStatus> = TODO()

}