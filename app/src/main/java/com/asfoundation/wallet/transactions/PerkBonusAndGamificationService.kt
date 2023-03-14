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
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.appcoins.wallet.gamification.repository.PromotionsRepository
import com.appcoins.wallet.gamification.repository.entity.GamificationStatus
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.main.PendingIntentNavigator
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.repository.TransactionRepositoryType
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.gamification.ReachedLevelInfo
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import com.appcoins.wallet.sharedpreferences.BackupTriggerPreferencesDataSource
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.pow

@AndroidEntryPoint
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

  @Inject
  lateinit var pendingIntentNavigator: PendingIntentNavigator

  private lateinit var notificationManager: NotificationManager

  @Inject
  lateinit var getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase

  @Inject
  lateinit var backupTriggerPreferences: BackupTriggerPreferencesDataSource

  @Deprecated("Deprecated in Java")
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

  private fun handleNotifications(address: String) = Single.zip(
    getLastShownLevelUp(address),
    promotionsRepository.getLastShownLevel(address, NOTIFICATIONS_ALMOST_NEXT_LEVEL),
    getGamificationStats(address),
    getLevelList(address),
    getNewTransactions(address)
  ) { lastShownLevel, almostNextLevelLastShown, statsPromotions, allLevels, transactions ->
    val bonusTransactionValue = getAllPerkBonusTransactionValues(transactions)
    if (isCaseInvalidForNotifications(statsPromotions, transactions, allLevels)) {
      handlePerkBonusNotificationPartnerUser(bonusTransactionValue)
      return@zip
    }
    val maxLevel = allLevels.maxByOrNull { level -> level.level }!!.level
    val currentLevel = statsPromotions.level
    val currentLevelStartAmount = allLevels[currentLevel].amount
    handlePerkAndLevelUpNotification(
      address = address,
      lastShownLevel = lastShownLevel,
      currentLevel = currentLevel,
      transactions = transactions,
      statsPromotions = statsPromotions,
      currentLevelStartAmount = currentLevelStartAmount,
      maxLevel = maxLevel,
      bonusTransactionValue = bonusTransactionValue
    )
    handleAlmostNextLevelNotification(
      address = address,
      almostNextLevelLastShown = almostNextLevelLastShown,
      currentLevel = currentLevel,
      statsPromotions = statsPromotions,
      currentLevelStartAmount = currentLevelStartAmount,
      maxLevel = maxLevel
    )
  }
    .doOnError { it.printStackTrace() }
    .subscribeOn(Schedulers.io())

  private fun handlePerkBonusNotificationPartnerUser(bonusTransactionValue: String) {
    if (bonusTransactionValue.isNotEmpty()) {
      buildNotification(
        createPerkBonusNotification(bonusTransactionValue),
        NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP
      )
    }
  }

  private fun handlePerkAndLevelUpNotification(
    address: String,
    lastShownLevel: Int,
    currentLevel: Int,
    transactions: List<Transaction>,
    statsPromotions: PromotionsGamificationStats,
    currentLevelStartAmount: BigDecimal,
    maxLevel: Int,
    bonusTransactionValue: String
  ) {
    if (lastShownLevel < currentLevel && hasPurchaseResultedInLevelUp(
        transactions,
        statsPromotions.totalSpend.minus(currentLevelStartAmount)
      )
    ) {
      backupTriggerPreferences.setTriggerState(
        walletAddress = address,
        active = true,
        triggerSource = BackupTriggerPreferencesDataSource.TriggerSource.NEW_LEVEL
      )
      promotionsRepository.shownLevel(address, currentLevel, NOTIFICATIONS_LEVEL_UP)
      buildNotification(
        createLevelUpNotification(
          statsPromotions = statsPromotions,
          maxLevelReached = currentLevel == maxLevel,
          levelUpBonusCredits = bonusTransactionValue
        ),
        NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP
      )
    } else if (bonusTransactionValue.isNotEmpty()) {
      buildNotification(
        createPerkBonusNotification(bonusTransactionValue),
        NOTIFICATION_SERVICE_ID_PERK_AND_LEVEL_UP
      )
    }
  }

  private fun handleAlmostNextLevelNotification(
    address: String,
    almostNextLevelLastShown: Int,
    currentLevel: Int,
    statsPromotions: PromotionsGamificationStats,
    currentLevelStartAmount: BigDecimal,
    maxLevel: Int
  ) {
    if (showAlmostNextLevelNotification(currentLevel, almostNextLevelLastShown, maxLevel)) {
      val totalAppCoinsAmountThisLevel =
        statsPromotions.nextLevelAmount!!.minus(currentLevelStartAmount)
      val currentAppCoinsAmountThisLevel = statsPromotions.totalSpend.minus(currentLevelStartAmount)

      when (statsPromotions.gamificationStatus) {
        GamificationStatus.APPROACHING_NEXT_LEVEL,
        GamificationStatus.APPROACHING_VIP_MAX -> {
          promotionsRepository.shownLevel(address, currentLevel, NOTIFICATIONS_ALMOST_NEXT_LEVEL)
          buildNotification(
            createAlmostNextLevelNotification(
              formatter.formatGamificationValues(
                totalAppCoinsAmountThisLevel
                  .minus(currentAppCoinsAmountThisLevel)
              )
            ),
            NOTIFICATION_SERVICE_ID_ALMOST_LEVEL_UP
          )
        }
        GamificationStatus.APPROACHING_VIP -> {
          promotionsRepository.shownLevel(address, currentLevel, NOTIFICATIONS_ALMOST_NEXT_LEVEL)
          buildNotification(
            createAlmostVipNotification(),
            NOTIFICATION_SERVICE_ID_ALMOST_LEVEL_UP
          )
        }
        else -> Unit
      }
    }
  }

  private fun showAlmostNextLevelNotification(
    currentLevel: Int,
    almostNextLevelLastShown: Int,
    maxLevel: Int
  ): Boolean = currentLevel in (almostNextLevelLastShown + 1) until maxLevel

  private fun getGamificationStats(address: String): Single<PromotionsGamificationStats> =
    getCurrentPromoCodeUseCase()
      .flatMapObservable { promotionsRepository.getGamificationStats(address, it.code) }
      .firstOrError()


  private fun getLastShownLevelUp(address: String): Single<Int> =
    promotionsRepository.getLastShownLevel(address, NOTIFICATIONS_LEVEL_UP)
      .map { if (it == PromotionsGamificationStats.INVALID_LEVEL) 0 else it }

  private fun getLevelList(address: String): Single<List<Levels.Level>> =
    promotionsRepository.getLevels(address)
      .lastOrError()
      .map { it.list }

  private fun isCaseInvalidForNotifications(
    statsPromotions: PromotionsGamificationStats,
    transactions: List<Transaction>,
    allLevels: List<Levels.Level>
  ): Boolean =
    statsPromotions.resultState != PromotionsGamificationStats.ResultState.OK ||
        !statsPromotions.isActive ||
        transactions.isEmpty() ||
        allLevels.isEmpty()

  private fun getNewTransactions(address: String): Single<List<Transaction>> =
    transactionRepository.fetchNewTransactions(address)
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

  private fun hasPurchaseResultedInLevelUp(
    transactions: List<Transaction>,
    currentLevelAmount: BigDecimal
  ): Boolean {
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

  private fun getPurchaseValueFromTransactions(transactions: List<Transaction>): String =
    transactions.find {
      it.type == Transaction.TransactionType.TOP_UP ||
          it.type == Transaction.TransactionType.IAP_OFFCHAIN
    }?.value ?: ""

  private fun buildNotification(
    notificationBuilder: NotificationCompat.Builder,
    notificationServiceId: Int
  ) = notificationManager.notify(notificationServiceId, notificationBuilder.build())

  private fun initializeNotificationBuilder(
    channelId: String,
    channelName: String,
    intent: PendingIntent
  ): NotificationCompat.Builder {
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
      .setSmallIcon(R.drawable.ic_appcoins_notification_icon)
  }

  private fun createPerkBonusNotification(value: String): NotificationCompat.Builder =
    initializeNotificationBuilder(
      PERK_CHANNEL_ID,
      PERK_CHANNEL_NAME,
      pendingIntentNavigator.getHomePendingIntent()
    )
      .setContentTitle(getString(R.string.perks_notification, value))
      .setContentText(getString(R.string.support_new_message_button))

  private fun createLevelUpNotification(
    statsPromotions: PromotionsGamificationStats,
    maxLevelReached: Boolean,
    levelUpBonusCredits: String
  ): NotificationCompat.Builder {
    val reachedLevelInfo: ReachedLevelInfo = if (maxLevelReached) {
      gamificationMapper.mapNotificationMaxLevelReached()
    } else {
      gamificationMapper.mapReachedLevelInfo(statsPromotions.level)
    }
    val builder =
      initializeNotificationBuilder(
        LEVEL_UP_CHANNEL_ID, LEVEL_UP_CHANNEL_NAME,
        pendingIntentNavigator.getPromotionsPendingIntent()
      )
        .setContentTitle(reachedLevelInfo.reachedTitle)
    val levelBitmap = reachedLevelInfo.planet?.toBitmap()
    if (levelBitmap != null) {
      builder.setLargeIcon(levelBitmap)
    }
    val bonusPercentage = DecimalFormat("##.#").format(statsPromotions.bonus) + "%"
    val contentMessage = when {
      maxLevelReached -> getString(R.string.gamification_how_max_level_body, bonusPercentage)
      levelUpBonusCredits.isEmpty() -> getString(
        R.string.gamification_leveled_up_notification_body,
        bonusPercentage
      )
      else -> getString(
        R.string.gamification_bonus_plus_perk_body,
        bonusPercentage,
        levelUpBonusCredits
      )
    }
    return builder.setContentText(contentMessage)
      .setStyle(NotificationCompat.BigTextStyle().bigText(contentMessage))
  }

  private fun createAlmostNextLevelNotification(appCoinsToSpend: String) =
    initializeNotificationBuilder(
      LEVEL_UP_CHANNEL_ID,
      LEVEL_UP_CHANNEL_NAME,
      pendingIntentNavigator.getPromotionsPendingIntent()
    )
      .setContentTitle(getString(R.string.gamification_level_up_notification_title))
      .setContentText(getString(R.string.gamification_level_up_notification_body, appCoinsToSpend))

  private fun createAlmostVipNotification() = initializeNotificationBuilder(
    LEVEL_UP_CHANNEL_ID,
    LEVEL_UP_CHANNEL_NAME,
    pendingIntentNavigator.getAlmostVipPendingIntent()
  )
    .setContentTitle(getString(R.string.vip_program_almost_notification_title))
    .setContentText(getString(R.string.vip_program_almost_notification_body))

  private fun getScaledValue(valueStr: String?): String? {
    if (valueStr == null) return null
    var value = BigDecimal(valueStr)
    value = removeEtherDecimals(value)
    if (value <= BigDecimal.ZERO) return null
    return formatter.formatGamificationValues(value)
  }

  private fun removeEtherDecimals(value: BigDecimal) = value.divide(
    BigDecimal(10.0.pow(C.ETHER_DECIMALS.toDouble())),
    2,
    RoundingMode.FLOOR
  )

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
      context.startService(
        Intent(context, PerkBonusAndGamificationService::class.java)
          .putExtra(ADDRESS_KEY, address)
      )
    }
  }
}
