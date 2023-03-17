package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.TransactionsApi
import com.appcoins.wallet.core.network.backend.model.WalletHistory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Single
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class OffChainTransactionsRepository @Inject constructor(private val api: TransactionsApi) {

  val objectMapper = ObjectMapper()
  val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

  fun getTransactionsSync(wallet: String, versionCode: String, startingDate: Long? = null,
                          endingDate: Long? = null, offset: Int, sort: String?,
                          limit: Int): Call<WalletHistory> {
    objectMapper.dateFormat = dateFormat //??
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) //??

    return api.transactionHistorySync(wallet, versionCode,
        startingDate = startingDate?.let { dateFormat.format(it) },
        endingDate = endingDate?.let { dateFormat.format(it) }, offset = offset, sort = sort,
        limit = limit, languageCode = Locale.getDefault().language)
  }

  fun getTransactionsById(wallet: String,
                          txList: List<String>): Single<List<WalletHistory.Transaction>> {
    return api.getTransactionsById(wallet, txList.toTypedArray())
  }
}
