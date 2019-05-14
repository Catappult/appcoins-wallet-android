package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.poa.BlockchainErrorMapper
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

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
    private val disposables: CompositeDisposable) :
    TransactionRepository(networkInfo, accountKeystoreService,
        defaultTokenProvider, errorMapper, nonceObtainer, scheduler) {
  override fun fetchTransaction(wallet: String): Observable<List<Transaction>> {

    disposables.add(offChainTransactions.getTransactions(wallet, false)
        .toObservable()
        .flatMapIterable { it }
        .map { mapper.map(it, wallet) }
        .toList()
        .doOnSuccess { localRepository.insertAll(it) }
        .subscribe({}, { it.printStackTrace() }))

    return localRepository.getAllAsFlowable(wallet)
        .map { mapper.map(it) }
        .toObservable()
        .distinctUntilChanged()
  }

  override fun stop() {
    disposables.clear()
  }
}