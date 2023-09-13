package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.TransactionsHistoryRepository
import javax.inject.Inject

class GetInvoiceByIdUseCase
@Inject
constructor(private val transactionRepository: TransactionsHistoryRepository) {
  operator fun invoke(invoiceId: String, ewt: String) =
    transactionRepository.getInvoiceUrl(invoiceId = invoiceId, ewt = ewt)
}
