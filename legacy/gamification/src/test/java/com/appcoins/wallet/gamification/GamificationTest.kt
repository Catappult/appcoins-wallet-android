package com.appcoins.wallet.gamification

import androidx.room.EmptyResultSetException
import com.appcoins.wallet.core.network.backend.model.*
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.entity.*
import com.appcoins.wallet.sharedpreferences.FiatCurrenciesPreferencesDataSource
import io.mockk.mockk
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.net.UnknownHostException
import java.util.*

class GamificationTest {
  private lateinit var gamification: Gamification
  private val api = GamificationApiTest()
  private val local = UserStatsDataTest()
  private val fiatCurrenciesPreferencesDataSource: FiatCurrenciesPreferencesDataSource = mockk(relaxed = true)
  private val formatter: CurrencyFormatUtils = CurrencyFormatUtils()
  private val date = Date()

  companion object {
    private const val WALLET = "wallet1"
    private const val PACKAGE_NAME = "packageName"
    private const val GAMIFICATION_ID = "GAMIFICATION"
  }

  @Before
  @Throws(Exception::class)
  fun setUp() {
    RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
    RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    gamification = Gamification(BdsPromotionsRepository(api, local, fiatCurrenciesPreferencesDataSource, formatter))
  }

  @Test
  fun getUserStats() {
    val userStatsGamification =
      GamificationResponse(
        id = "GAMIFICATION",
        priority = 100,
        gamificationStatus = GamificationStatus.STANDARD,
        bonus = 2.2,
        totalSpend = BigDecimal.ONE,
        totalEarned = BigDecimal.ZERO,
        level = 1,
        nextLevelAmount = BigDecimal.TEN,
        status = PromotionsResponse.Status.ACTIVE,
        bundle = true
      )
    val referralResponse =
      ReferralResponse(
        id = "REFERRAL",
        priority = 99,
        gamificationStatus = GamificationStatus.NONE,
        maxAmount = BigDecimal(2.2),
        available = 3,
        bundle = true,
        completed = 2,
        currency = "EUR",
        symbol = "€",
        invited = false,
        link = "link",
        pendingAmount = BigDecimal.ONE,
        receivedAmount = BigDecimal.ZERO,
        userStatus = ReferralResponse.UserStatus.REDEEMED,
        minAmount = BigDecimal.ZERO,
        status = PromotionsResponse.Status.ACTIVE,
        amount = BigDecimal.ONE
      )
    local.walletOriginResponse = Single.just(WalletOrigin.UNKNOWN)
    local.userStatusResponse = Single.just(emptyList())
    api.userStatusResponse =
      Single.just(
        UserStatusResponse(
          listOf(userStatsGamification, referralResponse), WalletOrigin.APTOIDE
        )
      )
    val testObserver = gamification.getUserStats(WALLET, null, true)
      .test()
    testObserver.assertResult(
      PromotionsGamificationStats(
        resultState = PromotionsGamificationStats.ResultState.UNKNOWN_ERROR,
        fromCache = true,
        gamificationStatus = GamificationStatus.NONE
      ),
      PromotionsGamificationStats(
        resultState = PromotionsGamificationStats.ResultState.OK,
        level = 1,
        nextLevelAmount = BigDecimal.TEN,
        bonus = 2.2,
        totalSpend = BigDecimal.ONE,
        totalEarned = BigDecimal.ZERO,
        isActive = true,
        fromCache = false,
        gamificationStatus = GamificationStatus.STANDARD
      )
    )
    testObserver.assertComplete()
  }

  @Test
  fun getUserStatsNoNetworkWithDb() {
    local.walletOriginResponse = Single.just(WalletOrigin.APTOIDE)
    local.userStatusResponse = Single.just(
      listOf(
        GamificationResponse(
          id = "GAMIFICATION",
          priority = 100,
          gamificationStatus = GamificationStatus.STANDARD,
          bonus = 15.0,
          totalSpend = BigDecimal(25000.0),
          totalEarned = BigDecimal(5000.0),
          level = 5,
          nextLevelAmount = BigDecimal(60000.0),
          status = PromotionsResponse.Status.ACTIVE,
          bundle = false
        )
      )
    )
    api.userStatusResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getUserStats(WALLET, null, true)
      .test()
    testObserver.assertResult(
      PromotionsGamificationStats(
        resultState = PromotionsGamificationStats.ResultState.OK,
        level = 5,
        nextLevelAmount = BigDecimal(60000.0),
        bonus = 15.0,
        totalSpend = BigDecimal(25000.0),
        totalEarned = BigDecimal(5000.0), isActive = true, fromCache = true,
        gamificationStatus = GamificationStatus.STANDARD
      ),
      PromotionsGamificationStats(
        resultState = PromotionsGamificationStats.ResultState.NO_NETWORK,
        level = PromotionsGamificationStats.INVALID_LEVEL,
        nextLevelAmount = BigDecimal.ZERO,
        bonus = -1.0,
        totalSpend = BigDecimal.ZERO,
        totalEarned = BigDecimal.ZERO,
        isActive = false,
        fromCache = false,
        gamificationStatus = GamificationStatus.NONE
      )
    )
  }

  @Test
  fun getUserStatsNoNetworkWithoutDb() {
    local.walletOriginResponse = Single.error(EmptyResultSetException(""))
    local.userStatusResponse = Single.error(EmptyResultSetException(""))
    api.userStatusResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getUserStats(WALLET, null, true)
      .test()
    testObserver.assertResult(
      PromotionsGamificationStats(
        resultState = PromotionsGamificationStats.ResultState.UNKNOWN_ERROR,
        level = -1,
        nextLevelAmount = BigDecimal.ZERO,
        bonus = -1.0,
        totalSpend = BigDecimal.ZERO,
        totalEarned = BigDecimal.ZERO,
        isActive = false,
        fromCache = true,
        gamificationStatus = GamificationStatus.NONE
      ),
      PromotionsGamificationStats(
        resultState = PromotionsGamificationStats.ResultState.NO_NETWORK,
        level = PromotionsGamificationStats.INVALID_LEVEL,
        nextLevelAmount = BigDecimal.ZERO,
        bonus = -1.0,
        totalSpend = BigDecimal.ZERO,
        totalEarned = BigDecimal.ZERO,
        isActive = false,
        fromCache = false,
        gamificationStatus = GamificationStatus.NONE
      )
    )
  }

  @Test
  fun getUserLevelWithLevelIncreasing() {
    local.walletOriginResponse = Single.just(WalletOrigin.APTOIDE)
    local.userStatusResponse = Single.just(
      listOf(
        GamificationResponse(
          id = "GAMIFICATION",
          priority = 100,
          gamificationStatus = GamificationStatus.STANDARD,
          bonus = 15.0,
          totalSpend = BigDecimal(25000.0),
          totalEarned = BigDecimal(5000.0),
          level = 5,
          nextLevelAmount = BigDecimal(60000.0),
          status = PromotionsResponse.Status.ACTIVE,
          bundle = false
        )
      )
    )
    val userStatsGamification =
      GamificationResponse(
        id = "GAMIFICATION",
        priority = 100,
        gamificationStatus = GamificationStatus.STANDARD,
        bonus = 2.2,
        totalSpend = BigDecimal.ONE,
        totalEarned = BigDecimal.ZERO,
        level = 4,
        nextLevelAmount = BigDecimal.TEN,
        status = PromotionsResponse.Status.ACTIVE,
        bundle = true
      )
    val referralResponse =
      ReferralResponse(
        id = "REFERRAL",
        priority = 99,
        gamificationStatus = GamificationStatus.STANDARD,
        maxAmount = BigDecimal(2.2),
        available = 3,
        bundle = true,
        completed = 2,
        currency = "EUR",
        symbol = "€",
        invited = false,
        link = "link",
        pendingAmount = BigDecimal.ONE,
        receivedAmount = BigDecimal.ZERO,
        userStatus = ReferralResponse.UserStatus.REDEEMED,
        minAmount = BigDecimal.ZERO,
        status = PromotionsResponse.Status.ACTIVE,
        amount = BigDecimal.ONE
      )
    api.userStatusResponse =
      Single.just(
        UserStatusResponse(
          listOf(userStatsGamification, referralResponse), WalletOrigin.APTOIDE
        )
      )
    val testObserver = gamification.getUserLevel(WALLET, null)
      .test()
    testObserver.assertValue(4)
  }

  @Test
  fun getUserLevelWithLevelDecreasing() {
    // uncommon use case - if gamification tables change and more spending is required to reach that
    //  level - the user level may decrease
    local.walletOriginResponse = Single.just(WalletOrigin.APTOIDE)
    local.userStatusResponse = Single.just(
      listOf(
        GamificationResponse(
          id = "GAMIFICATION",
          priority = 100,
          gamificationStatus = GamificationStatus.STANDARD,
          bonus = 15.0,
          totalSpend = BigDecimal(25000.0),
          totalEarned = BigDecimal(5000.0),
          level = 4,
          nextLevelAmount = BigDecimal(60000.0),
          status = PromotionsResponse.Status.ACTIVE,
          bundle = false
        )
      )
    )
    val userStatsGamification =
      GamificationResponse(
        id = "GAMIFICATION",
        priority = 100,
        gamificationStatus = GamificationStatus.STANDARD,
        bonus = 2.2,
        totalSpend = BigDecimal.ONE,
        totalEarned = BigDecimal.ZERO,
        level = 5,
        nextLevelAmount = BigDecimal.TEN,
        status = PromotionsResponse.Status.ACTIVE,
        bundle = true
      )
    val referralResponse =
      ReferralResponse(
        id = "REFERRAL",
        priority = 99,
        gamificationStatus = GamificationStatus.STANDARD,
        maxAmount = BigDecimal(2.2),
        available = 3,
        bundle = true,
        completed = 2,
        currency = "EUR",
        symbol = "€",
        invited = false,
        link = "link",
        pendingAmount = BigDecimal.ONE,
        receivedAmount = BigDecimal.ZERO,
        userStatus = ReferralResponse.UserStatus.REDEEMED,
        minAmount = BigDecimal.ZERO,
        status = PromotionsResponse.Status.ACTIVE,
        amount = BigDecimal.ONE
      )
    api.userStatusResponse =
      Single.just(
        UserStatusResponse(
          listOf(userStatsGamification, referralResponse), WalletOrigin.APTOIDE
        )
      )
    val testObserver = gamification.getUserLevel(WALLET, null)
      .test()
    testObserver.assertValue(5)
  }

  @Test
  fun getUserLevelWhenGamificationBecomesUnavailable() {
    // uncommon use case - the user may no longer have gamification available (e.g. wallet origin
    //  changes). We want to display always the most recent emission, so we should make sure that
    //  the invalid level value is received, even if user had gamification previously
    local.walletOriginResponse = Single.just(WalletOrigin.APTOIDE)
    local.userStatusResponse = Single.just(
      listOf(
        GamificationResponse(
          id = "GAMIFICATION",
          priority = 100,
          gamificationStatus = GamificationStatus.STANDARD,
          bonus = 15.0,
          totalSpend = BigDecimal(25000.0),
          totalEarned = BigDecimal(5000.0),
          level = 4,
          nextLevelAmount = BigDecimal(60000.0),
          status = PromotionsResponse.Status.ACTIVE,
          bundle = false
        )
      )
    )
    api.userStatusResponse =
      Single.just(UserStatusResponse(emptyList(), WalletOrigin.PARTNER))
    val testObserver = gamification.getUserLevel(WALLET, null)
      .test()
    testObserver.assertValue(PromotionsGamificationStats.INVALID_LEVEL)
  }

  @Test
  fun getUserLevelNoNetworkWithDb() {
    local.walletOriginResponse = Single.just(WalletOrigin.APTOIDE)
    local.userStatusResponse = Single.just(
      listOf(
        GamificationResponse(
          id = "GAMIFICATION",
          priority = 100,
          gamificationStatus = GamificationStatus.STANDARD,
          bonus = 15.0,
          totalSpend = BigDecimal(25000.0),
          totalEarned = BigDecimal(5000.0),
          level = 5,
          nextLevelAmount = BigDecimal(60000.0),
          status = PromotionsResponse.Status.ACTIVE,
          bundle = false
        )
      )
    )
    api.userStatusResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getUserLevel(WALLET, null)
      .test()
    testObserver.assertValue(5)
  }

  @Test
  fun getUserLevelNoNetworkWithoutGamificationInDb() {
    local.walletOriginResponse = Single.just(WalletOrigin.UNKNOWN)
    local.userStatusResponse = Single.just(emptyList())
    api.userStatusResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getUserLevel(WALLET, null)
      .test()
    testObserver.assertValue(PromotionsGamificationStats.INVALID_LEVEL)
  }

  @Test
  fun getLevels() {
    local.levelsResponse = Single.just(
      LevelsResponse(
        listOf(Level(BigDecimal.ONE, 5.0, 1), Level(BigDecimal.TEN, 10.0, 2)),
        LevelsResponse.Status.ACTIVE, date
      )
    )
    api.levelsResponse = Single.just(
      LevelsResponse(
        listOf(Level(BigDecimal.ONE, 2.0, 1), Level(BigDecimal.TEN, 20.0, 2)),
        LevelsResponse.Status.ACTIVE, date
      )
    )
    val testObserver = gamification.getLevels(WALLET)
      .test()
    testObserver.assertResult(
      Levels(
        Levels.Status.OK,
        listOf(Levels.Level(BigDecimal.ONE, 5.0, 1), Levels.Level(BigDecimal.TEN, 10.0, 2)),
        true, date, true
      ), Levels(
        Levels.Status.OK,
        listOf(Levels.Level(BigDecimal.ONE, 2.0, 1), Levels.Level(BigDecimal.TEN, 20.0, 2)),
        true, date, false
      )
    )
  }

  @Test
  fun getLevelsNoNetworkWithDb() {
    local.levelsResponse = Single.just(
      LevelsResponse(
        listOf(Level(BigDecimal.ONE, 5.0, 1), Level(BigDecimal.TEN, 10.0, 2)),
        LevelsResponse.Status.ACTIVE, date
      )
    )
    api.levelsResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getLevels(WALLET)
      .test()
    testObserver.assertResult(
      Levels(
        Levels.Status.OK,
        listOf(Levels.Level(BigDecimal.ONE, 5.0, 1), Levels.Level(BigDecimal.TEN, 10.0, 2)),
        true, date, true
      ),
      Levels(Levels.Status.NO_NETWORK, emptyList(), false, null, false)
    )
  }

  @Test
  fun getLevelsNoNetworkWithoutDb() {
    local.levelsResponse = Single.error(EmptyResultSetException(""))
    api.levelsResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getLevels(WALLET)
      .test()
    testObserver.assertResult(
      Levels(Levels.Status.UNKNOWN_ERROR, emptyList(), false, null, true),
      Levels(Levels.Status.NO_NETWORK, emptyList(), false, null, false)
    )
  }

  // TODO - remove the next 3 tests once everything has been put in offline first logic.
  @Test
  fun getLevelsOld() {
    api.levelsResponse = Single.just(
      LevelsResponse(
        listOf(Level(BigDecimal.ONE, 2.0, 1), Level(BigDecimal.TEN, 20.0, 2)),
        LevelsResponse.Status.ACTIVE, date
      )
    )
    val testObserver = gamification.getLevels(WALLET, false)
      .test()
    testObserver.assertValue(
      Levels(
        Levels.Status.OK,
        listOf(Levels.Level(BigDecimal.ONE, 2.0, 1), Levels.Level(BigDecimal.TEN, 20.0, 2)),
        true, date
      )
    )
  }

  @Test
  fun getLevelsNoNetworkWithDbOld() {
    api.levelsResponse = Single.error(UnknownHostException())
    local.levelsResponse = Single.just(
      LevelsResponse(
        listOf(Level(BigDecimal.ONE, 5.0, 1), Level(BigDecimal.TEN, 10.0, 2)),
        LevelsResponse.Status.ACTIVE, date
      )
    )
    val testObserver = gamification.getLevels(WALLET, false)
      .test()
    testObserver.assertValue(
      Levels(
        Levels.Status.OK,
        listOf(Levels.Level(BigDecimal.ONE, 5.0, 1), Levels.Level(BigDecimal.TEN, 10.0, 2)),
        true, date, true
      )
    )
  }

  @Test
  fun getLevelsNoNetworkWithoutDbOld() {
    api.levelsResponse = Single.error(UnknownHostException())
    local.levelsResponse = Single.error(EmptyResultSetException(""))
    val testObserver = gamification.getLevels(WALLET, false)
      .test()
    testObserver.assertValue(Levels(Levels.Status.NO_NETWORK))
  }

  @Test
  fun getBonus() {
    api.bonusResponse = Single.just(
      ForecastBonusResponse(BigDecimal.ONE, 0, ForecastBonusResponse.Status.ACTIVE, "")
    )
    val testObserver = gamification.getEarningBonus(
      wallet = WALLET,
      packageName = PACKAGE_NAME,
      amount = BigDecimal.ONE,
      promoCodeString = "",
      currency = null,
      paymentMethodId = null
    )
      .test()
    testObserver.assertValue(ForecastBonus(ForecastBonus.Status.ACTIVE, BigDecimal.ONE))
  }

  @Test
  fun getBonusNoNetwork() {
    api.bonusResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getEarningBonus(
      wallet = WALLET,
      packageName = PACKAGE_NAME,
      amount = BigDecimal.ONE,
      promoCodeString = "",
      currency = null,
      paymentMethodId = null
    )
      .test()
    testObserver.assertValue(ForecastBonus(ForecastBonus.Status.NO_NETWORK))
  }

  @Test
  fun hasNewLevelNoNewLevel() {
    val userStatsGamification =
      GamificationResponse(
        id = GAMIFICATION_ID,
        priority = 100,
        gamificationStatus = GamificationStatus.STANDARD,
        bonus = 2.2,
        totalSpend = BigDecimal.ONE,
        totalEarned = BigDecimal.ZERO,
        level = 0,
        nextLevelAmount = BigDecimal.TEN,
        status = PromotionsResponse.Status.ACTIVE,
        bundle = true
      )

    local.lastShownLevelResponse = Single.just(0)
    val test = gamification.hasNewLevel(
      WALLET, GamificationContext.SCREEN_MY_LEVEL,
      userStatsGamification.level
    )
      .test()
    test.assertValue(false)
      .assertNoErrors()
      .assertComplete()
  }

  @Test
  fun hasNewLevelNewLevel() {
    val userStatsGamification =
      GamificationResponse(
        id = GAMIFICATION_ID,
        priority = 100,
        gamificationStatus = GamificationStatus.STANDARD,
        bonus = 2.2,
        totalSpend = BigDecimal.ONE,
        totalEarned = BigDecimal.ZERO,
        level = 0,
        nextLevelAmount = BigDecimal.TEN,
        status = PromotionsResponse.Status.ACTIVE,
        bundle = true
      )

    local.lastShownLevelResponse = Single.just(PromotionsGamificationStats.INVALID_LEVEL)
    val test = gamification.hasNewLevel(
      WALLET, GamificationContext.SCREEN_MY_LEVEL,
      userStatsGamification.level
    )
      .test()
    test.assertValue(true)
      .assertNoErrors()
      .assertComplete()
  }

  @Test
  fun levelShown() {
    val shownLevel = 1
    val test = gamification.levelShown(WALLET, shownLevel, GamificationContext.SCREEN_MY_LEVEL)
      .test()
    test.assertNoErrors()
      .assertComplete()
    local.lastShownLevelResponse!!.test()
      .assertValue(shownLevel)
    Assert.assertEquals("the updated wallet was not the expected one", WALLET, local.getWallet())
  }

}