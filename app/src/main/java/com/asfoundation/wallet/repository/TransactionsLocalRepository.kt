package com.asfoundation.wallet.repository

import android.content.SharedPreferences
import com.asfoundation.wallet.repository.entity.LastUpdatedWalletEntity
import com.asfoundation.wallet.repository.entity.TransactionEntity
import com.asfoundation.wallet.repository.entity.TransactionLinkIdEntity
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject
@BoundTo(supertype = TransactionsRepository::class)
class TransactionsLocalRepository @Inject constructor(private val transactionsDao: TransactionsDao,
                                                      private val sharedPreferences: SharedPreferences,
                                                      private val transactionLinkIdDao: TransactionLinkIdDao) :
    TransactionsRepository {

  companion object {
    private const val OLD_TRANSACTIONS_LOAD = "IS_OLD_TRANSACTIONS_LOADED"
    private const val LAST_LOCALE = "locale"
  }

  override fun getAllAsFlowable(relatedWallet: String): Flowable<List<TransactionEntity>> {
    return transactionsDao.getAllAsFlowable(relatedWallet)
  }

  override fun getWalletLastUpdated(wallet: String): Flowable<LastUpdatedWalletEntity> {
    return transactionsDao.getLastUpdatedWallet(wallet)
  }

  override fun setTransactionsLastUpdated(wallet: String, timestamp: Long) {
    return transactionsDao.insertLastUpdatedWallet(LastUpdatedWalletEntity(wallet, timestamp))
  }


  override fun insertAll(roomTransactions: List<TransactionEntity>) {
    return transactionsDao.insertAll(roomTransactions)
  }

  override fun getNewestTransaction(relatedWallet: String): Maybe<TransactionEntity> {
    return transactionsDao.getNewestTransaction(relatedWallet)

  }

  override fun getOlderTransaction(relatedWallet: String): Maybe<TransactionEntity> {
    return transactionsDao.getOlderTransaction(relatedWallet)
  }

  override fun isOldTransactionsLoaded(): Single<Boolean> {
    return Single.fromCallable { sharedPreferences.getBoolean(OLD_TRANSACTIONS_LOAD, false) }
  }

  override fun deleteAllTransactions() = transactionsDao.deleteAllTransactions()

  override fun oldTransactionsLoaded() {
    sharedPreferences.edit()
        .putBoolean(OLD_TRANSACTIONS_LOAD, true)
        .apply()
  }

  override fun setLocale(locale: String) {
    sharedPreferences.edit()
        .putString(LAST_LOCALE, locale)
        .apply()
  }

  override fun getLastLocale() = sharedPreferences.getString(LAST_LOCALE, null)

  override fun insertTransactionLink(revertTxId: String, originalTxId: String) {
    val transactionLinkId = TransactionLinkIdEntity(null, revertTxId, originalTxId)
    transactionLinkIdDao.insert(transactionLinkId)
  }

  override fun getRevertedTransaction(wallet: String, txId: String): Single<TransactionEntity> {
    return transactionLinkIdDao.getRevertedTransaction(txId)
        .flatMap { link -> transactionsDao.getById(wallet, link.linkTransactionId) }
  }

  override fun getRevertTransaction(wallet: String, txId: String): Single<TransactionEntity> {
    return transactionLinkIdDao.getRevertTransaction(txId)
        .flatMap { link -> transactionsDao.getById(wallet, link.transactionId) }
  }

  override fun getRevertedTxId(wallet: String, txId: String): Single<String> {
    return transactionLinkIdDao.getRevertedTransaction(txId)
        .map { it.linkTransactionId }
  }
}