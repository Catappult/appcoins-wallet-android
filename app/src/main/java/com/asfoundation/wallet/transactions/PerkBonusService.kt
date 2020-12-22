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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function4
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
      // this sleep is to wait for all transactions (including perk bonuses) to be registered
      // when there are several perk bonuses associated to the same transaction
      // it can take a bit of time before every transaction is inserted in DB
      Thread.sleep(TRANSACTION_TIME_WAIT_FOR_ALL)
      handleNotifications(it)
    }
  }

  private fun handleNotifications(address: String) {
    disposables.add(Single.zip(promotionsRepository.getLastShownLevel(address,
        GamificationScreen.NOTIFICATIONS.toString()).map { if (it < 0) 0 else it },
        promotionsRepository.getGamificationStats(address), promotionsRepository.getLevels(address),
        Single.just(getAllPerksBonusTransactionValue(getNewTransactions(address))),
        Function4 { lastShownLevel: Int, gamificationStats: GamificationStats,
                    allLevels: Levels, bonusTransactionValue: String ->
          val maxBonus = allLevels.list.last().bonus
          val currentLevel = gamificationStats.level
          if (gamificationStats.status == GamificationStats.Status.OK &&
              lastShownLevel < currentLevel) {
            promotionsRepository.shownLevel(address, currentLevel,
                GamificationScreen.NOTIFICATIONS.toString())
            if (bonusTransactionValue.isEmpty()) {
              buildNotification(createLevelUpNotification(gamificationStats, maxBonus),
                  NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP)
            } else {
              buildNotification(
                  createLevelUpNotification(gamificationStats, maxBonus, bonusTransactionValue),
                  NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP)
            }
          } else if (bonusTransactionValue.isNotEmpty()) {
            buildNotification(createPerkBonusNotification(bonusTransactionValue),
                NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP)
          }
          // TODO only do this if max level hasn't been reached and
          //  hasn't sent almost there notification for current level
          val almostNextLevelPercent = GamificationMapper(this)
              .mapAlmostNextLevelUpPercentage(gamificationStats.level)
          val currLevelStartAmount = allLevels.list[currentLevel].amount
          val totalAppCoinsAmountThisLevel = gamificationStats.nextLevelAmount!!.minus(
              currLevelStartAmount)
          val currentAppCoinsAmountThisLevel = gamificationStats.totalSpend.minus(
              currLevelStartAmount)
          if (totalAppCoinsAmountThisLevel > BigDecimal.ZERO &&
              currentAppCoinsAmountThisLevel > BigDecimal.ZERO &&
              (currentAppCoinsAmountThisLevel.toDouble() / totalAppCoinsAmountThisLevel.toDouble()
                  * 100.0).toInt() > almostNextLevelPercent) {
            buildNotification(createAlmostNextLevelNotification(formatter.formatGamificationValues(
                totalAppCoinsAmountThisLevel.minus(currentAppCoinsAmountThisLevel)),maxBonus),
                NOTIFICATION_SERVICE_ID_ALMOST_LEVEL_UP)
          }
        }).subscribe({}, { it.printStackTrace() }))
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

  private fun getAllPerksBonusTransactionValue(transactions: List<Transaction>): String {
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
                                notficationServiceId: Int) =
      notificationManager.notify(notficationServiceId, notificationBuilder.build())

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

  private fun initializeBaseLevelUpNotification(gamificationStats: GamificationStats,
                                                maxBonus: Double): NotificationCompat.Builder {
    val reachedLevelInfo: ReachedLevelInfo =
        GamificationMapper(this).mapReachedLevelInfo(gamificationStats.level)
    val builder =
        initializeNotificationBuilder(LEVEL_UP_CHANNEL_ID, LEVEL_UP_CHANNEL_NAME,
            PendingIntent.getActivity(this, 0,
                GamificationActivity.newIntent(this, maxBonus), 0))
            .setContentTitle(reachedLevelInfo.reachedTitle)
    val planet = reachedLevelInfo.planet?.toBitmap()
    return if (planet != null) builder.setLargeIcon(planet) else builder
  }

  private fun createLevelUpNotification(gamificationStats: GamificationStats, maxBonus: Double):
      NotificationCompat.Builder = initializeBaseLevelUpNotification(gamificationStats, maxBonus)
      .setContentText(getString(R.string.gamification_leveled_up_notification_body,
          gamificationStats.bonus.toString()))

  private fun createLevelUpNotification(gamificationStats: GamificationStats, maxBonus: Double,
                                        levelUpBonusCredits: String):
      NotificationCompat.Builder = initializeBaseLevelUpNotification(gamificationStats, maxBonus)
      .setContentText(getString(R.string.gamification_leveled_up_notification_body,
          levelUpBonusCredits))

  private fun createAlmostNextLevelNotification(appCoinsToSpend: String, maxBonus: Double):
      NotificationCompat.Builder = initializeNotificationBuilder(ALMOST_NEXT_LEVEL_CHANNEL_ID,
      ALMOST_NEXT_LEVEL_CHANNEL_NAME,
      PendingIntent.getActivity(this, 0,
          GamificationActivity.newIntent(this, maxBonus), 0))
      .setContentTitle(getString(R.string.gamification_level_up_notification_title))
      .setContentText(getString(R.string.gamification_level_up_notification_body, appCoinsToSpend))

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
    disposables.clear()
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
    private const val ALMOST_NEXT_LEVEL_CHANNEL_ID =
        "notification_channel_gamification_almost_next_level"
    private const val ALMOST_NEXT_LEVEL_CHANNEL_NAME = "Almost Next Level Notification Channel"

    @JvmStatic
    fun buildService(context: Context, address: String) {
      Intent(context, PerkBonusService::class.java).also { intent ->
        intent.putExtra(ADDRESS_KEY, address)
        context.startService(intent)
      }
    }
  }
}