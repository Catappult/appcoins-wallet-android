package com.asfoundation.wallet.transactions

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.repository.TransactionRepositoryType
import com.asfoundation.wallet.ui.TransactionsActivity
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.gamification.ReachedLevelInfo
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.toBitmap
import dagger.android.AndroidInjection
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.pow

class PerkBonusService : IntentService(PerkBonusService::class.java.simpleName) {

  @Inject
  lateinit var transactionRepository: TransactionRepositoryType

  @Inject
  lateinit var promotionsRepository: PromotionsRepository

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val disposables = CompositeDisposable()

  private lateinit var notificationManager: NotificationManager

  override fun onCreate() {
    super.onCreate()
    AndroidInjection.inject(this)
  }

  override fun onHandleIntent(intent: Intent?) {
    val address = intent?.getStringExtra(ADDRESS_KEY)
    address?.let {
      notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      handleNotification(it)
    }
  }

  private fun handleNotification(address: String) {
    disposables.add(Single.zip(promotionsRepository.getLastShownLevel(address,
        GamificationScreen.NOTIFICATIONS.toString()).map { if (it < 0) 0 else it },
        promotionsRepository.getGamificationStats(address),
        Single.just(getPerkBonusTransactionValue(address)),
        Function3 { lastShownLevel: Int, gamificationStats: GamificationStats, bonusTrasactionValue: String ->
          if (gamificationStats.status == GamificationStats.Status.OK && lastShownLevel < gamificationStats.level) {
            promotionsRepository.shownLevel(address, gamificationStats.level,
                GamificationScreen.NOTIFICATIONS.toString())
            buildNotification(createLevelUpNotification(gamificationStats))
            // TODO - build notification will be changed depending on the kind of notification
            //  to build (with level up; with perks; with both; with "almost reaching level")
            // TODO - check if needs to send perk notification in another method (changing this method's name)
          } else if (bonusTrasactionValue.isNotEmpty()) {
            buildNotification(createPerkNotification(bonusTrasactionValue))
          }
          // TODO - check if we need to put subscribeOn schedulers.io() and observeOn mainThread or something similar
        }).subscribe())
  }

  private fun getPerkBonusTransactionValue(address: String, timesCalled: Int = 0): String {
    try {
      val transactions = transactionRepository.fetchNewTransactions(address)
          .blockingGet()
      return if (transactions.isEmpty() && timesCalled < 4) {
        getPerkBonusTransactionValue(address, timesCalled + 1)
      } else {
        getPerkBonusTransactionValue(transactions)
      }
    } catch (exception: Exception) {
      exception.printStackTrace()
    }
    return ""
  }

  private fun buildNotification(notificationBuilder: NotificationCompat.Builder) {
    notificationManager.notify(NOTIFICATION_SERVICE_ID, notificationBuilder.build())
  }

  private fun createPositiveNotificationIntent(): PendingIntent {
    val intent = TransactionsActivity.newIntent(this)
    return PendingIntent.getActivity(this, 0, intent, 0)
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

  private fun initializeNotificationBuilder(channelId: String): NotificationCompat.Builder {
    val positiveIntent = createPositiveNotificationIntent()
    val builder: NotificationCompat.Builder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channelName: CharSequence = "Notification channel"
      val importance = NotificationManager.IMPORTANCE_HIGH
      val notificationChannel =
          NotificationChannel(channelId, channelName, importance)
      builder = NotificationCompat.Builder(this, channelId)
      notificationManager.createNotificationChannel(notificationChannel)
    } else {
      builder = NotificationCompat.Builder(this, channelId)
      builder.setVibrate(LongArray(0))
    }
    return builder.setAutoCancel(true)
        .setContentIntent(positiveIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
  }

  private fun createPerkNotification(value: String): NotificationCompat.Builder {
    return initializeNotificationBuilder(PERK_CHANNEL_ID).setContentTitle(
        getString(R.string.perks_notification, value))
        .setContentText(getString(R.string.support_new_message_button))
  }

  private fun createLevelUpNotification(
      gamificationStats: GamificationStats): NotificationCompat.Builder {
    val reachedLevelInfo: ReachedLevelInfo =
        GamificationMapper(this).mapReachedLevelInfo(gamificationStats.level)
    val builder = initializeNotificationBuilder(LEVEL_UP_CHANNEL_ID).setContentTitle(
        reachedLevelInfo.reachedTitle)
        .setContentText(getString(R.string.gamification_leveled_up_notification_body,
            gamificationStats.bonus.toString()))
    val planet = reachedLevelInfo.planet?.toBitmap()
    return if (planet != null) builder.setLargeIcon(planet) else builder
  }

  private fun getScaledValue(valueStr: String?): String? {
    if (valueStr == null) return null
    var value = BigDecimal(valueStr)
    value = value.divide(BigDecimal(10.0.pow(C.ETHER_DECIMALS.toDouble())), 2, RoundingMode.FLOOR)
    if (value <= BigDecimal.ZERO) return null
    return formatter.formatGamificationValues(value)
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
  }

  companion object {
    private const val NOTIFICATION_SERVICE_ID = 77796
    private const val ADDRESS_KEY = "ADDRESS_KEY"
    private const val PERK_CHANNEL_ID = "notification_channel_perk_bonus"
    private const val LEVEL_UP_CHANNEL_ID = "notification_channel_gamification_level_up"

    @JvmStatic
    fun buildService(context: Context, address: String) {
      Intent(context, PerkBonusService::class.java).also { intent ->
        intent.putExtra(ADDRESS_KEY, address)
        context.startService(intent)
      }
    }
  }
}