package com.asfoundation.wallet.ui.transactions

import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.Typed3EpoxyController
import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.transactions.models.DateModel_
import com.asfoundation.wallet.ui.transactions.models.PerkModel_
import com.asfoundation.wallet.ui.transactions.models.TransactionModel_
import com.asfoundation.wallet.ui.widget.entity.TransactionsModel
import com.asfoundation.wallet.util.CurrencyFormatUtils
import java.util.*
import kotlin.collections.HashSet

class TransactionsController : Typed3EpoxyController<TransactionsModel, Wallet, NetworkInfo>(
    EpoxyAsyncUtil.getAsyncBackgroundHandler(), EpoxyAsyncUtil.getAsyncBackgroundHandler()) {

  private val formatter = CurrencyFormatUtils()

  var transactionClickListener: ((Transaction) -> Unit)? = null

  override fun buildModels(txModel: TransactionsModel, wallet: Wallet, networkInfo: NetworkInfo) {
    // Add the transaction list and non-repeated dates
    // Note that it assumes that the transaction list is already ordered
    val dateHashSet = HashSet<Long>()
    for (transaction in txModel.transactions) {
      // Add date if it hasn't been added yet
      val date = roundTimeStamp(transaction.timeStamp)
      if (!dateHashSet.contains(date.time)) {
        add(
            DateModel_()
                .id(date.time)
                .date(date)
        )
        dateHashSet.add(date.time)
      }
      // Add the transaction
      if (transaction.subType == Transaction.SubType.PERK_PROMOTION) {
        add(
            PerkModel_()
                .id(transaction.transactionId)
                .transaction(transaction)
                .clickListener(transactionClickListener)
        )
      } else {
        add(
            TransactionModel_()
                .id(transaction.transactionId)
                .tx(transaction)
                .defaultAddress(wallet.address)
                .currency(networkInfo.symbol)
                .formatter(formatter)
                .clickListener(transactionClickListener)
        )
      }
    }
  }

  private fun roundTimeStamp(timeStampInSec: Long): Date {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = timeStampInSec
    calendar[Calendar.MILLISECOND] = 999
    calendar[Calendar.SECOND] = 59
    calendar[Calendar.MINUTE] = 59
    calendar[Calendar.HOUR_OF_DAY] = 23
    return calendar.time
  }
}