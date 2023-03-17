package com.asfoundation.wallet.repository

import com.asfoundation.wallet.base.RxSchedulers
import com.asfoundation.wallet.entity.NetworkInfo
import com.appcoins.wallet.core.network.backend.model.WalletHistory
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.repository.entity.TransactionEntity
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionsMapper
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import it.czerwinski.android.hilt.annotations.BoundTo
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@BoundTo(supertype = TransactionRepositoryType::class)
class BackendTransactionRepository @Inject constructor(
  networkInfo: NetworkInfo,
  accountKeystoreService: AccountKeystoreService,
  defaultTokenProvider: DefaultTokenProvider,
  errorMapper: BlockchainErrorMapper,
  nonceObtainer: MultiWalletNonceObtainer,
  private val offChainTransactions: OffChainTransactions,
  private val localRepository: TransactionsRepository,
  private val mapper: TransactionMapper,
  private val transactionsMapper: TransactionsMapper,
  private val disposables: CompositeDisposable,
  private val rxSchedulers: RxSchedulers
) :
  TransactionRepository(
    networkInfo, accountKeystoreService,
    defaultTokenProvider, errorMapper, nonceObtainer, rxSchedulers
  ) {

  private lateinit var disposable: Disposable
  override fun fetchTransaction(wallet: String): Observable<List<Transaction>> {
    if (!::disposable.isInitialized || disposable.isDisposed) {
      disposable = getLastProcessedTime(wallet)
        .subscribeOn(rxSchedulers.io)
        .flatMapObservable { startingDate ->
          return@flatMapObservable Observable.merge(
            fetchNewTransactions(wallet, startingDate = startingDate),
            fetchMissingOldTransactions(wallet)
          )
        }
        .buffer(2, TimeUnit.SECONDS)
        .flatMap { transactions -> saveTransactions(transactions.flatten(), wallet) }
        .onErrorResumeNext(saveTransactions(emptyList(), wallet))
        .subscribe({}, { it.printStackTrace() })
    }
    disposables.add(disposable)

    // getWalletLastUpdated ensures that we only start listening to DB once it gets updated from
    // network at least once
    return localRepository.getWalletLastUpdated(wallet)
      .flatMap { localRepository.getAllAsFlowable(wallet) }
      .flatMap { transactions -> getLinkedTransactions(wallet, transactions) }
      .map { transactions -> orderByDateDescending(transactions) }
      .toObservable()
      .distinctUntilChanged()
  }

  private fun orderByDateDescending(transactions: List<Transaction>): List<Transaction> {
    return transactions.sortedByDescending { tx -> tx.timeStamp }
  }

  override fun fetchNewTransactions(wallet: String): Single<List<Transaction>> {
    return localRepository.getNewestTransaction(wallet)
      .map { it.processedTime }
      .defaultIfEmpty(0)
      .subscribeOn(rxSchedulers.io)
      .flatMapSingle { startingDate ->
        //We need +1 otherwise since the transaction on the backend is stored with 6 milliseconds
        // and we store with 3, so the last transaction will always be returned
        fetchNewTransactions(wallet, startingDate + 1).firstOrError()
      }
      .flatMap { transactions -> saveTransactions(transactions, wallet).firstOrError() }
      .map { it.map { transaction -> mapper.map(transaction) } }
  }

  private fun getLinkedTransactions(
    wallet: String,
    transactions: List<TransactionEntity>
  ): Flowable<List<Transaction>> {
    return Observable.fromIterable(transactions)
      .flatMapSingle { transaction ->
        if (isRevertTransaction(transaction.type)) {
          localRepository.getRevertedTransaction(wallet, transaction.transactionId)
            .map { link -> mapper.map(transaction, link) }
            .onErrorResumeNext {
              //if getRevertedTransaction fails it means that the original transaction doesn't
              //exist in the database hence we need to fetch it from the remote repo to be able
              //to use it in the transactions flow
              localRepository.getRevertedTxId(wallet, transaction.transactionId)
                .flatMap { link ->
                  offChainTransactions.getTransactionsById(wallet, link)
                    .map { result -> result[link] }
                    .map { revertedTx -> transactionsMapper.map(revertedTx, wallet) }
                    .doOnSuccess { revertedTx ->
                      localRepository.insertAll(listOf(revertedTx))
                    }
                    .map { revertedTx -> mapper.map(transaction, revertedTx) }
                }
                .onErrorReturn { mapper.map(transaction) }
            }
        } else {
          localRepository.getRevertTransaction(wallet, transaction.transactionId)
            .map { link -> mapper.map(transaction, link) }
            .onErrorReturn { mapper.map(transaction) }
        }
      }
      .toList()
      .toFlowable()
  }

  private fun fetchNewTransactions(
    wallet: String,
    startingDate: Long
  ): Observable<MutableList<WalletHistory.Transaction>> {
    var sort = OffChainTransactions.Sort.DESC
    if (startingDate != 0L) {
      sort = OffChainTransactions.Sort.ASC
    }
    return fetchTransactions(wallet, startingDate = startingDate, sort = sort)
  }

  private fun fetchMissingOldTransactions(
    wallet: String
  ): Observable<MutableList<WalletHistory.Transaction>> {
    return localRepository.isOldTransactionsLoaded()
      .flatMapObservable { isLoaded ->
        if (isLoaded) {
          return@flatMapObservable Observable.empty<MutableList<WalletHistory.Transaction>>()
        }
        return@flatMapObservable localRepository.getOlderTransaction(wallet)
          .map { it.processedTime }
          .flatMapObservable {
            fetchTransactions(wallet, 0L, it, OffChainTransactions.Sort.DESC)
          }
          .doOnComplete { localRepository.oldTransactionsLoaded() }
      }

  }

  private fun fetchTransactions(
    wallet: String,
    startingDate: Long? = null,
    endDate: Long? = null,
    sort: OffChainTransactions.Sort? = null
  ): Observable<MutableList<WalletHistory.Transaction>> {
    return TransactionsLoadObservable(offChainTransactions, wallet, startingDate, endDate, sort)
      .flatMapSingle { transactions ->
        Observable.fromIterable(transactions)
          .toList()
      }
  }

  private fun saveTransactions(
    transactions: List<WalletHistory.Transaction>,
    wallet: String
  ): Observable<List<TransactionEntity>> {
    return Completable.fromAction {
      localRepository.setTransactionsLastUpdated(wallet, Date().time)
    }
      .andThen(Observable.fromIterable(transactions))
      .flatMap { transaction ->
        if (isRevertTransaction(transaction.type, transaction.linkedTx.orEmpty())) {
          transaction.linkedTx?.forEach {
            localRepository.insertTransactionLink(transaction.txID, it)
          }
        }
        val entity = transactionsMapper.map(transaction, wallet)
        Observable.just(entity)
      }
      .toList()
      .doOnSuccess { localRepository.insertAll(it) }
      .toObservable()
  }

  private fun getLastProcessedTime(wallet: String): Maybe<Long> {
    val lastLocale = localRepository.getLastLocale()
    val currentLocale = Locale.getDefault().language
    return if (lastLocale == null || lastLocale == currentLocale) {
      if (lastLocale == null) localRepository.setLocale(currentLocale)
      localRepository.getNewestTransaction(wallet)
        .map { it.processedTime }
        .defaultIfEmpty(0)
    } else {
      Maybe.fromCallable {
        localRepository.setLocale(currentLocale)
        localRepository.deleteAllTransactions()
        0L
      }
    }
  }

  private fun isRevertTransaction(type: String, links: List<String>): Boolean {
    val isRevertType = transactionsMapper.isRevertType(type)
    return isRevertType && links.isNotEmpty()
  }

  private fun isRevertTransaction(transactionEntity: TransactionEntity.TransactionType): Boolean {
    return transactionEntity == TransactionEntity.TransactionType.IAP_REVERT ||
        transactionEntity == TransactionEntity.TransactionType.BONUS_REVERT ||
        transactionEntity == TransactionEntity.TransactionType.TOP_UP_REVERT
  }

  override fun stop() = disposables.clear()
}