package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.TransactionsHistoryRepository
import javax.inject.Inject

class GetInvoiceByIdUseCase @Inject constructor(
  private val transactionRepository: TransactionsHistoryRepository
) {

  operator fun invoke(invoiceId: String) =
    transactionRepository.getInvoiceUrl(invoiceId = invoiceId)

}
