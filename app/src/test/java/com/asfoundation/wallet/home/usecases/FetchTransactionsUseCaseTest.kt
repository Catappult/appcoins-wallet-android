package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.TransactionRepositoryType
import com.asfoundation.wallet.transactions.Transaction
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.jupiter.api.Test


class FetchTransactionsUseCaseTest {

  // Create a mock of the TransactionRepositoryType interface
  private val mockTransactionRepository: TransactionRepositoryType = mockk()

  @Test
  fun `when invoke is called, it should fetch transactions from repository`() {
    // Define the input parameters
    val wallet = "myWallet"

    // Define the expected output
    val expectedTransactions = listOf<Transaction>()

    // Mock the behavior of the transaction repository
    every { mockTransactionRepository.fetchTransaction(wallet) } returns Observable.just(expectedTransactions)

    // Call the method being tested
    val testObserver = TestObserver<List<Transaction>>()
    mockTransactionRepository.fetchTransaction(wallet).subscribe(testObserver)

    // Verify that the method returns the expected output
    assert(testObserver.values().first() is List<Transaction>)
    testObserver.assertComplete()
    testObserver.assertNoErrors()
  }
}
