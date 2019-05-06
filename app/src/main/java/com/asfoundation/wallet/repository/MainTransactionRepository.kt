package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.RawTransaction
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.poa.BlockchainErrorMapper
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.TransactionsNetworkClientType
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.transactions.TransactionsMapper
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single

class MainTransactionRepository(
    private val networkInfo: NetworkInfo,
    accountKeystoreService: AccountKeystoreService,
    private val inDiskCache: TransactionLocalSource,
    private val blockExplorerClient: TransactionsNetworkClientType,
    defaultTokenProvider: DefaultTokenProvider,
    errorMapper: BlockchainErrorMapper,
    nonceObtainer: MultiWalletNonceObtainer,
    scheduler: Scheduler,
    private val mapper: TransactionsMapper
) : TransactionRepository(networkInfo, accountKeystoreService,
    defaultTokenProvider, errorMapper, nonceObtainer, scheduler) {

  override fun fetchTransaction(wallet: Wallet): Observable<List<Transaction>> {
    return Single.merge(fetchFromCache(networkInfo, wallet),
        fetchAndCacheFromNetwork(networkInfo, wallet))
        .flatMapSingle { mapper.map(it) }
        .toObservable()
  }

  private fun fetchFromCache(networkInfo: NetworkInfo,
                             wallet: Wallet): Single<List<RawTransaction>> {
    return inDiskCache.fetchTransaction(networkInfo, wallet)
  }

  private fun fetchAndCacheFromNetwork(networkInfo: NetworkInfo,
                                       wallet: Wallet): Single<List<RawTransaction>> {
    return inDiskCache.findLast(networkInfo, wallet)
        .flatMap { lastTransaction ->
          Single.fromObservable(
              blockExplorerClient.fetchLastTransactions(wallet, lastTransaction, networkInfo))
        }
        .onErrorResumeNext { throwable ->
          Single.fromObservable(
              blockExplorerClient.fetchLastTransactions(wallet, null, networkInfo))
        }
        .flatMapCompletable { transactions ->
          inDiskCache.putTransactions(networkInfo, wallet, transactions)
        }
        .andThen(inDiskCache.fetchTransaction(networkInfo, wallet))
  }
}