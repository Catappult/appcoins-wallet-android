package com.asfoundation.wallet.transactions

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.repository.TransactionRepositoryType
import com.asfoundation.wallet.ui.TransactionsActivity
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.Scheduler
import java.math.BigDecimal
import kotlin.math.pow

class PerkBonusRepository(private val context: Context,
                          private val notificationManager: NotificationManager,
                          private val transactionRepository: TransactionRepositoryType,
                          private val formatter: CurrencyFormatUtils,
                          private val scheduler: Scheduler) {

  @SuppressLint("CheckResult")
  fun handlePerkTransactionNotification(address: String, timesCalled: Int = 0) {
    transactionRepository.fetchNewTransactions(address)
        .subscribeOn(scheduler)
        .doOnSuccess {
          if (it.isEmpty() && timesCalled < 4) {
            handlePerkTransactionNotification(address, timesCalled + 1)
          }
        }
        .filter { it.isNotEmpty() }
        .map { getPerkBonusTransactionValue(it) }
        .filter { it.isNotEmpty() }
        .doOnSuccess { buildNotification(it) }
        .subscribe({}, { it.printStackTrace() })
  }

  private fun buildNotification(value: String) {
    val notificationBuilder = createNotification(value).build()
    notificationManager.notify(NOTIFICATION_SERVICE_ID, notificationBuilder)
  }

  private fun createPositiveNotificationIntent(): PendingIntent {
    val intent = TransactionsActivity.newIntent(context)
    return PendingIntent.getActivity(context, 0, intent, 0)
  }

  private fun getPerkBonusTransactionValue(transactions: List<Transaction>): String {
    //Empty validation is done in done before on the filter
    val lastTransactionTime = transactions[0].processedTime
    //To avoid older transactions that may have not yet been inserted in DB we give a small gap
    val transactionGap = lastTransactionTime - 15000
    val transaction =
        transactions.takeWhile { it.processedTime >= transactionGap }
            .find { it.subType == Transaction.SubType.PERK_PROMOTION }
    return getScaledValue(transaction?.value) ?: ""
  }

  private fun createNotification(value: String): NotificationCompat.Builder {
    val positiveIntent = createPositiveNotificationIntent()
    val builder: NotificationCompat.Builder
    val channelId = "notification_channel_perk_bonus"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channelName: CharSequence = "Notification channel"
      val importance = NotificationManager.IMPORTANCE_HIGH
      val notificationChannel =
          NotificationChannel(channelId, channelName, importance)
      builder = NotificationCompat.Builder(context, channelId)
      notificationManager.createNotificationChannel(notificationChannel)
    } else {
      builder = NotificationCompat.Builder(context, channelId)
      builder.setVibrate(LongArray(0))
    }
    return builder.setContentTitle("You've earned $value APPC")
        .setAutoCancel(true)
        .setContentIntent(positiveIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentText(context.getString(R.string.support_new_message_button))
  }

  private fun getScaledValue(valueStr: String?): String? {
    if (valueStr == null) return null
    val walletCurrency = WalletCurrency.CREDITS
    var value = BigDecimal(valueStr)
    value = value.divide(BigDecimal(10.0.pow(C.ETHER_DECIMALS.toDouble())))
    return formatter.formatCurrency(value, walletCurrency)
  }

  companion object {
    private const val NOTIFICATION_SERVICE_ID = 77796
  }
}
