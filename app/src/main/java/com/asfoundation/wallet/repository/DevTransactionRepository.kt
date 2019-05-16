package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.poa.BlockchainErrorMapper
import com.asfoundation.wallet.repository.entity.TransactionEntity
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class DevTransactionRepository(
    networkInfo: NetworkInfo,
    accountKeystoreService: AccountKeystoreService,
    defaultTokenProvider: DefaultTokenProvider,
    errorMapper: BlockchainErrorMapper,
    nonceObtainer: MultiWalletNonceObtainer,
    scheduler: Scheduler,
    private val offChainTransactions: OffChainTransactions,
    private val localRepository: TransactionsDao,
    private val mapper: TransactionMapper,
    private val disposables: CompositeDisposable,
    private val ioScheduler: Scheduler,
    private val dateFormatter: DateFormatter) :
    TransactionRepository(networkInfo, accountKeystoreService,
        defaultTokenProvider, errorMapper, nonceObtainer, scheduler) {

  lateinit var disposable: Disposable
  override fun fetchTransaction(wallet: String): Observable<List<Transaction>> {
    if (!::disposable.isInitialized || disposable.isDisposed) {
      disposable = localRepository.getLastTransaction(wallet)
          .map { it.timeStamp }
          .defaultIfEmpty(0)
          .subscribeOn(ioScheduler)
          .flatMapObservable { timeStamp ->
            getTransactions(wallet, dateFormatter.format(timeStamp))
                .buffer(2, TimeUnit.SECONDS)
                .doOnNext {
                  localRepository.insertAll(it.flatten())
                }
          }
          .subscribe({}, { it.printStackTrace() })
    }
    disposables.add(disposable)

    return localRepository.getAllAsFlowable(wallet)
        .map { mapper.map(it) }
        .toObservable()
        .distinctUntilChanged()
  }

  private fun getTransactions(wallet: String,
                              startingDate: String? = null): Observable<MutableList<TransactionEntity>> {
    return TransactionsLoadObservable(offChainTransactions, wallet, startingDate)
        .flatMapSingle { transactions ->
          Observable.fromIterable(transactions)
              .map { mapper.map(it, wallet) }
              .toList()
        }
  }

  override fun stop() {
    disposables.clear()
  }
}