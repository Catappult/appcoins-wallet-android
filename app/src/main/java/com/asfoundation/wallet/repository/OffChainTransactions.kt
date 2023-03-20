package com.asfoundation.wallet.repository

import android.annotation.SuppressLint
import com.appcoins.wallet.core.network.backend.model.WalletHistory
import com.asf.wallet.BuildConfig
import io.reactivex.Single
import retrofit2.HttpException
import javax.inject.Inject

class OffChainTransactions @Inject constructor(
    private val repository: OffChainTransactionsRepository) {

  private val versionCode: String
    get() = BuildConfig.VERSION_CODE.toString()

  fun getTransactions(wallet: String, startingDate: Long? = null,
                      endingDate: Long? = null, offset: Int, sort: Sort?,
                      limit: Int = 10): MutableList<WalletHistory.Transaction> {
    @SuppressLint("DefaultLocale") val lowerCaseSort = sort?.name?.toLowerCase()
    val transactions =
        repository.getTransactionsSync(wallet, versionCode, startingDate, endingDate, offset,
            sort = lowerCaseSort, limit = limit)
            .execute()
    val body = transactions.body()
    if (transactions.isSuccessful && body != null) {
      return body.result
    }
    throw HttpException(transactions)
  }

  fun getTransactionsById(wallet: String,
                          txId: String): Single<Map<String, WalletHistory.Transaction>> {
    return repository.getTransactionsById(wallet, listOf(txId))
        .map { transactions -> transactions.associateBy({ it.txID }, { it }) }
  }

  enum class Sort {
    ASC, DESC
  }
}
