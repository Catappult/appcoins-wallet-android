package com.asfoundation.wallet.transactions

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.appcoins.wallet.gamification.GamificationContext.NOTIFICATIONS_ALMOST_NEXT_LEVEL
import com.appcoins.wallet.gamification.GamificationContext.NOTIFICATIONS_LEVEL_UP
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.promotions.PromotionsActivity
import com.asfoundation.wallet.repository.TransactionRepositoryType
import com.asfoundation.wallet.ui.TransactionsActivity
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.gamification.ReachedLevelInfo
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.toBitmap
import dagger.android.AndroidInjection
import io.reactivex.Single
import io.reactivex.functions.Function5
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.pow

class PerkBonusAndGamificationService :
    IntentService(PerkBonusAndGamificationService::class.java.simpleName) {

  @Inject
  lateinit var transactionRepository: TransactionRepositoryType

  @Inject
  lateinit var promotionsRepository: PromotionsRepository

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var gamificationMapper: GamificationMapper

  private lateinit var notificationManager: NotificationManager

  override fun onCreate() {
    super.onCreate()
    AndroidInjection.inject(this)
  }

  override fun onHandleIntent(intent: Intent?) {
    val address = intent?.getStringExtra(ADDRESS_KEY)
    address?.let {
      notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      // this sleep is to wait for all transactions (including perk bonuses) to be registered.
      // When there are several bonuses associated to the same transaction
      // it can take a bit of time before every one of them to be processed by backend
      Thread.sleep(TRANSACTION_TIME_WAIT_FOR_ALL_IN_MILLIS)
      // blocking so that service does not call onDestroy beforehand
      handleNotifications(it).blockingGet()
    }
  }

  private fun handleNotifications(address: String): Single<Unit> {
    return Single.zip(getLastShownLevelUp(address),
        promotionsRepository.getLastShownLevel(address, NOTIFICATIONS_ALMOST_NEXT_LEVEL),
        getGamificationStats(address),
        getLevelList(address),
        getNewTransactions(address),
        Function5 { lastShownLevel: Int, almostNextLevelLastShown: Int, stats: GamificationStats,
                    allLevels: List<Levels.Level>, transactions: List<Transaction> ->
          val bonusTransactionValue = getAllPerkBonusTransactionValues(transactions)
          if (isCaseInvalidForNotifications(stats, transactions, allLevels)) {
            handlePerkBonusNotificationPartnerUser(bonusTransactionValue)
            return@Function5
          }
          val maxLevel = allLevels.maxBy { level -> level.level }!!.level
          val currentLevel = stats.level
          val currentLevelStartAmount = allLevels[currentLevel].amount
          handlePerkAndLevelUpNotification(address, lastShownLevel, currentLevel, transactions,
              stats, currentLevelStartAmount, maxLevel, bonusTransactionValue)
          handleAlmostNextLevelNotification(address, almostNextLevelLastShown, currentLevel, stats,
              currentLevelStartAmount, maxLevel)
        })
        .doOnError { it.printStackTrace() }
        .subscribeOn(Schedulers.io())
  }

  private fun handlePerkBonusNotificationPartnerUser(bonusTransactionValue: String) {
    if (bonusTransactionValue.isNotEmpty()) {
      buildNotification(createPerkBonusNotification(bonusTransactionValue),
          NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP)
    }
  }

  private fun handlePerkAndLevelUpNotification(address: String, lastShownLevel: Int,
                                               currentLevel: Int, transactions: List<Transaction>,
                                               stats: GamificationStats,
                                               currentLevelStartAmount: BigDecimal,
                                               maxLevel: Int, bonusTransactionValue: String) {
    if (lastShownLevel < currentLevel && hasPurchaseResultedInLevelUp(transactions,
            stats.totalSpend.minus(currentLevelStartAmount))) {
      promotionsRepository.shownLevel(address, currentLevel, NOTIFICATIONS_LEVEL_UP)
      buildNotification(createLevelUpNotification(stats,
          currentLevel == maxLevel, bonusTransactionValue),
          NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP)
    } else if (bonusTransactionValue.isNotEmpty()) {
      buildNotification(createPerkBonusNotification(bonusTransactionValue),
          NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP)
    }
  }

  private fun handleAlmostNextLevelNotification(address: String, almostNextLevelLastShown: Int,
                                                currentLevel: Int, stats: GamificationStats,
                                                currentLevelStartAmount: BigDecimal,
                                                maxLevel: Int) {
    if (showAlmostNextLevelNotification(currentLevel, almostNextLevelLastShown, maxLevel)) {
      val almostNextLevelPercent = gamificationMapper.mapAlmostNextLevelUpPercentage(stats.level)
      val totalAppCoinsAmountThisLevel = stats.nextLevelAmount!!.minus(currentLevelStartAmount)
      val currentAppCoinsAmountThisLevel = stats.totalSpend.minus(currentLevelStartAmount)
      if (isNearNextLevel(totalAppCoinsAmountThisLevel, currentAppCoinsAmountThisLevel,
              almostNextLevelPercent)) {
        promotionsRepository.shownLevel(address, currentLevel, NOTIFICATIONS_ALMOST_NEXT_LEVEL)
        buildNotification(createAlmostNextLevelNotification(
            formatter.formatGamificationValues(totalAppCoinsAmountThisLevel
                .minus(currentAppCoinsAmountThisLevel))), NOTIFICATION_SERVICE_ID_ALMOST_LEVEL_UP)
      }
    }
  }

  private fun showAlmostNextLevelNotification(currentLevel: Int, almostNextLevelLastShown: Int,
                                              maxLevel: Int): Boolean {
    return currentLevel in (almostNextLevelLastShown + 1) until maxLevel
  }

  private fun isNearNextLevel(totalAppCoinsAmountThisLevel: BigDecimal,
                              currentAppCoinsAmountThisLevel: BigDecimal,
                              almostNextLevelPercent: Int): Boolean {
    return totalAppCoinsAmountThisLevel > BigDecimal.ZERO &&
        currentAppCoinsAmountThisLevel > BigDecimal.ZERO &&
        (currentAppCoinsAmountThisLevel.toDouble() / totalAppCoinsAmountThisLevel.toDouble()
            * 100.0).toInt() > almostNextLevelPercent
  }

  private fun getGamificationStats(address: String): Single<GamificationStats> =
      promotionsRepository.getGamificationStats(address)
          .lastOrError()

  private fun getLastShownLevelUp(address: String): Single<Int> {
    return promotionsRepository.getLastShownLevel(address, NOTIFICATIONS_LEVEL_UP)
        .map { if (it == GamificationStats.INVALID_LEVEL) 0 else it }
  }

  private fun getLevelList(address: String): Single<List<Levels.Level>> =
      promotionsRepository.getLevels(address)
          .lastOrError()
          .map { it.list }

  private fun isCaseInvalidForNotifications(stats: GamificationStats,
                                            transactions: List<Transaction>,
                                            allLevels: List<Levels.Level>): Boolean {
    return stats.status != GamificationStats.Status.OK || !stats.isActive ||
        transactions.isEmpty() || allLevels.isEmpty()
  }

  private fun getNewTransactions(address: String): Single<List<Transaction>> {
    return transactionRepository.fetchNewTransactions(address)
        .map {
          //note that - if list is empty, then will retry again (if max retries wasn't reached)
          //Use small gap, to avoid older transactions that may have not yet been inserted in DB
          val transactionGap = it[0].processedTime - TRANSACTION_GAP_TIME_IN_MILLIS
          it.takeWhile { transaction -> transaction.processedTime >= transactionGap }
          it
        }
        .doOnError { it.printStackTrace() }
        .retry(4)
        .onErrorReturn { emptyList() }
  }

  private fun getAllPerkBonusTransactionValues(transactions: List<Transaction>): String {
    if (transactions.isEmpty())
      return ""
    var appcValue = BigDecimal.ZERO
    transactions.forEach {
      if (it.subType == Transaction.SubType.PERK_PROMOTION) {
        appcValue += BigDecimal(it.value)
      }
    }
    return getScaledValue(appcValue.toString()) ?: ""
  }

  private fun hasPurchaseResultedInLevelUp(transactions: List<Transaction>,
                                           currentLevelAmount: BigDecimal): Boolean {
    // this method is used for the cases where there is no information in shared preferences
    //  about the last level where the level up notification has been shown
    // As such, one will compare the actual purchase values that weren't made with APPC-C
    //  (the ones that resulted in bonus) and check if it was above the APPC spent of current level
    val isThereBonus = transactions.any { it.type == Transaction.TransactionType.BONUS }
    val purchaseValue = getPurchaseValueFromTransactions(transactions)
    if (purchaseValue.isEmpty() || !isThereBonus) {
      return false
    }
    val purchaseNumericValue = BigDecimal(purchaseValue)
    return !(purchaseNumericValue <= BigDecimal.ZERO ||
        removeEtherDecimals(purchaseNumericValue) < currentLevelAmount)
  }

  private fun getPurchaseValueFromTransactions(transactions: List<Transaction>): String {
    return transactions.find {
      it.type == Transaction.TransactionType.TOP_UP ||
          it.type == Transaction.TransactionType.IAP_OFFCHAIN
    }?.value ?: ""
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

  private fun createLevelUpNotification(stats: GamificationStats, maxLevelReached: Boolean,
                                        levelUpBonusCredits: String):
      NotificationCompat.Builder {
    val reachedLevelInfo: ReachedLevelInfo = if (maxLevelReached) {
      gamificationMapper.mapNotificationMaxLevelReached()
    } else {
      gamificationMapper.mapReachedLevelInfo(stats.level)
    }
    val builder =
        initializeNotificationBuilder(LEVEL_UP_CHANNEL_ID, LEVEL_UP_CHANNEL_NAME,
            PendingIntent.getActivity(this, 0,
                PromotionsActivity.newIntent(this), 0))
            .setContentTitle(reachedLevelInfo.reachedTitle)
    val levelBitmap = reachedLevelInfo.planet?.toBitmap()
    if (levelBitmap != null) {
      builder.setLargeIcon(levelBitmap)
    }
    val bonusPercentage = DecimalFormat("##.#").format(stats.bonus) + "%"
    val contentMessage = when {
      maxLevelReached -> {
        getString(R.string.gamification_how_max_level_body, bonusPercentage)
      }
      levelUpBonusCredits.isEmpty() -> {
        getString(R.string.gamification_leveled_up_notification_body, bonusPercentage)
      }
      else -> {
        getString(R.string.gamification_bonus_plus_perk_body, bonusPercentage, levelUpBonusCredits)
      }
    }
    return builder.setContentText(contentMessage)
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText(contentMessage))
  }

  private fun createAlmostNextLevelNotification(appCoinsToSpend: String):
      NotificationCompat.Builder {
    return initializeNotificationBuilder(LEVEL_UP_CHANNEL_ID,
        LEVEL_UP_CHANNEL_NAME, PendingIntent.getActivity(this, 0,
        PromotionsActivity.newIntent(this), 0))
        .setContentTitle(getString(R.string.gamification_level_up_notification_title))
        .setContentText(
            getString(R.string.gamification_level_up_notification_body, appCoinsToSpend))
  }

  private fun getScaledValue(valueStr: String?): String? {
    if (valueStr == null) return null
    var value = BigDecimal(valueStr)
    value = removeEtherDecimals(value)
    if (value <= BigDecimal.ZERO) return null
    return formatter.formatGamificationValues(value)
  }

  private fun removeEtherDecimals(value: BigDecimal) = value.divide(BigDecimal(10.0.pow(
      C.ETHER_DECIMALS.toDouble())), 2, RoundingMode.FLOOR)

  companion object {
    private const val TRANSACTION_GAP_TIME_IN_MILLIS = 15000L
    private const val TRANSACTION_TIME_WAIT_FOR_ALL_IN_MILLIS = 500L
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