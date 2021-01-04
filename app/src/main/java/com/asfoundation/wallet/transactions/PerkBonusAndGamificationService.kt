package com.asfoundation.wallet.transactions

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.repository.TransactionRepositoryType
import com.asfoundation.wallet.ui.TransactionsActivity
import com.asfoundation.wallet.ui.gamification.GamificationActivity
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.gamification.ReachedLevelInfo
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.toBitmap
import dagger.android.AndroidInjection
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function5
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.pow

class PerkBonusAndGamificationService : IntentService(
    PerkBonusAndGamificationService::class.java.simpleName) {

  @Inject
  lateinit var transactionRepository: TransactionRepositoryType

  @Inject
  lateinit var promotionsRepository: PromotionsRepository

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var gamificationMapper: GamificationMapper

  private lateinit var disposable: Disposable

  private lateinit var notificationManager: NotificationManager

  override fun onCreate() {
    super.onCreate()
    AndroidInjection.inject(this)
  }

  override fun onHandleIntent(intent: Intent?) {
    val address = intent?.getStringExtra(ADDRESS_KEY)
    address?.let {
      notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      // this sleep is to wait for all transactions (including perk bonuses) to be registered
      // when there are several perk bonuses associated to the same transaction
      // it can take a bit of time before every transaction is inserted in DB
      Thread.sleep(TRANSACTION_TIME_WAIT_FOR_ALL)
      handleNotifications(it)
    }
  }

  private fun handleNotifications(address: String) {
    disposable = Single.zip(promotionsRepository.getLastShownLevel(address,
        GamificationContext.NOTIFICATIONS_LEVEL_UP.toString()).map { if (it < 0) 0 else it },
        promotionsRepository.getLastShownLevel(address,
            GamificationContext.NOTIFICATIONS_ALMOST_NEXT_LEVEL.toString()),
        promotionsRepository.getGamificationStats(address), promotionsRepository.getLevels(address),
        Single.just(getAllPerkBonusTransactionValues(getNewTransactions(address))),
        Function5 { lastShownLevel: Int, almostNextLevelLastShown: Int, stats: GamificationStats,
                    allLevels: Levels, bonusTransactionValue: String ->
          val maxBonus = allLevels.list.maxBy { level -> level.bonus }!!.bonus
          val maxLevel = allLevels.list.maxBy { level -> level.level }!!.level
          val currentLevel = stats.level
          if (stats.status == GamificationStats.Status.OK &&
              lastShownLevel < currentLevel) {
            promotionsRepository.shownLevel(address, currentLevel,
                GamificationContext.NOTIFICATIONS_LEVEL_UP.toString())
            buildNotification(
                createLevelUpNotification(stats, maxBonus,
                    currentLevel == maxLevel, bonusTransactionValue),
                NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP)
          } else if (bonusTransactionValue.isNotEmpty()) {
            buildNotification(createPerkBonusNotification(bonusTransactionValue),
                NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP)
          }
          if (currentLevel in (almostNextLevelLastShown + 1) until maxLevel) {
            // the current level being shown is the value to be stored regarding if the notification
            // for almost reaching next level (current level + 1) has been shown or not
            val almostNextLevelPercent = gamificationMapper
                .mapAlmostNextLevelUpPercentage(stats.level)
            val currLevelStartAmount = allLevels.list[currentLevel].amount
            val totalAppCoinsAmountThisLevel = stats.nextLevelAmount!!.minus(
                currLevelStartAmount)
            val currentAppCoinsAmountThisLevel = stats.totalSpend.minus(
                currLevelStartAmount)
            if (totalAppCoinsAmountThisLevel > BigDecimal.ZERO &&
                currentAppCoinsAmountThisLevel > BigDecimal.ZERO &&
                (currentAppCoinsAmountThisLevel.toDouble() / totalAppCoinsAmountThisLevel.toDouble()
                    * 100.0).toInt() > almostNextLevelPercent) {
              promotionsRepository.shownLevel(address, currentLevel,
                  GamificationContext.NOTIFICATIONS_ALMOST_NEXT_LEVEL.toString())
              buildNotification(createAlmostNextLevelNotification(
                  formatter.formatGamificationValues(totalAppCoinsAmountThisLevel
                      .minus(currentAppCoinsAmountThisLevel)), maxBonus),
                  NOTIFICATION_SERVICE_ID_ALMOST_LEVEL_UP)
            }
          }
        }).subscribe({}, { it.printStackTrace() })
  }

  private fun getNewTransactions(address: String, timesCalled: Int = 0): List<Transaction> {
    try {
      val transactions = transactionRepository.fetchNewTransactions(address).blockingGet()
      return if (transactions.isEmpty() && timesCalled < 4) {
        getNewTransactions(address, timesCalled + 1)
      } else {
        val lastTransactionTime = transactions[0].processedTime
        //To avoid older transactions that may have not yet been inserted in DB we give a small gap
        val transactionGap = lastTransactionTime - TRANSACTION_GAP_TIME
        transactions.takeWhile { it.processedTime >= transactionGap }
        return transactions
      }
    } catch (exception: Exception) {
      exception.printStackTrace()
    }
    return emptyList()
  }

  private fun getAllPerkBonusTransactionValues(transactions: List<Transaction>): String {
    try {
      var accValue = BigDecimal.ZERO
      transactions.forEach {
        if (it.subType == Transaction.SubType.PERK_PROMOTION) {
          accValue += BigDecimal(it.value)
        }
      }
      return getScaledValue(accValue.toString()) ?: ""
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return ""
  }

  private fun buildNotification(notificationBuilder: NotificationCompat.Builder,
                                notificationServiceId: Int) =
      notificationManager.notify(notificationServiceId, notificationBuilder.build())

  private fun initializeNotificationBuilder(channelId: String, channelName: String,
                                            intent: PendingIntent):
      NotificationCompat.Builder {
    val builder: NotificationCompat.Builder
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        .setContentIntent(intent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
  }

  private fun createPerkBonusNotification(value: String): NotificationCompat.Builder {
    return initializeNotificationBuilder(PERK_CHANNEL_ID, PERK_CHANNEL_NAME,
        PendingIntent.getActivity(this, 0,
            TransactionsActivity.newIntent(this), 0))
        .setContentTitle(getString(R.string.perks_notification, value))
        .setContentText(getString(R.string.support_new_message_button))
  }

  private fun createLevelUpNotification(stats: GamificationStats, maxBonus: Double,
                                        maxLevelReached: Boolean, levelUpBonusCredits: String):
      NotificationCompat.Builder {
    val reachedLevelInfo: ReachedLevelInfo = if (maxLevelReached)
      gamificationMapper.mapNotificationMaxLevelReached()
    else gamificationMapper.mapReachedLevelInfo(stats.level)
    val builder =
        initializeNotificationBuilder(LEVEL_UP_CHANNEL_ID, LEVEL_UP_CHANNEL_NAME,
            PendingIntent.getActivity(this, 0,
                GamificationActivity.newIntent(this, maxBonus), 0))
            .setContentTitle(reachedLevelInfo.reachedTitle)
    val levelBitmap = reachedLevelInfo.planet?.toBitmap()
    if (levelBitmap != null) {
      builder.setLargeIcon(levelBitmap)
    }
    val contentMessage: String = if (levelUpBonusCredits.isEmpty() || maxLevelReached) {
      getString(R.string.gamification_leveled_up_notification_body,
          stats.bonus.toString())
    } else {
      // TODO change this content message once the proper string exists
      getString(R.string.gamification_leveled_up_notification_body,
          levelUpBonusCredits)
    }
    return builder.setContentText(contentMessage)
        .setStyle(NotificationCompat.BigTextStyle().bigText(contentMessage))
  }

  private fun createAlmostNextLevelNotification(appCoinsToSpend: String, maxBonus: Double):
      NotificationCompat.Builder {
    return initializeNotificationBuilder(LEVEL_UP_CHANNEL_ID,
        LEVEL_UP_CHANNEL_NAME, PendingIntent.getActivity(this, 0,
        GamificationActivity.newIntent(this, maxBonus), 0))
        .setContentTitle(getString(R.string.gamification_level_up_notification_title))
        .setContentText(
            getString(R.string.gamification_level_up_notification_body, appCoinsToSpend))
  }

  private fun getScaledValue(valueStr: String?): String? {
    if (valueStr == null) return null
    var value = BigDecimal(valueStr)
    value = value.divide(BigDecimal(10.0.pow(C.ETHER_DECIMALS.toDouble())), 2,
        RoundingMode.FLOOR)
    if (value <= BigDecimal.ZERO) return null
    return formatter.formatGamificationValues(value)
  }

  override fun onDestroy() {
    super.onDestroy()
    if (!disposable.isDisposed) {
      disposable.dispose()
    }
  }

  companion object {
    private const val TRANSACTION_GAP_TIME = 15000L
    private const val TRANSACTION_TIME_WAIT_FOR_ALL = 500L
    private const val NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP = 77796
    private const val NOTIFICATION_SERVICE_ID_ALMOST_LEVEL_UP = 77797
    private const val ADDRESS_KEY = "ADDRESS_KEY"
    private const val PERK_CHANNEL_ID = "notification_channel_perk_bonus"
    private const val PERK_CHANNEL_NAME = "Promotion Bonuses Notification Channel"
    private const val LEVEL_UP_CHANNEL_NAME = "Level Up Notification Channel"
    private const val LEVEL_UP_CHANNEL_ID = "notification_channel_gamification_level_up"

    @JvmStatic
    fun buildService(context: Context, address: String) {
      Intent(context, PerkBonusAndGamificationService::class.java).also { intent ->
        intent.putExtra(ADDRESS_KEY, address)
        context.startService(intent)
      }
    }
  }
}