package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.entity.*
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.net.UnknownHostException
import java.util.*

class GamificationTest {
  private lateinit var gamification: Gamification
  private val api = GamificationApiTest()
  private val local = GamificationLocalDataTest()
  private val date = Date()

  companion object {
    private const val WALLET = "wallet1"
    private const val PACKAGE_NAME = "packageName"
    private const val GAMIFICATION_ID = "GAMIFICATION"
    private const val REFERRAL_ID = "REFERRAL"
  }

  @Before
  @Throws(Exception::class)
  fun setUp() {
    gamification = Gamification(BdsPromotionsRepository(api, local))
  }

  @Test
  fun getUserStatsTest() {
    val userStatsGamification =
        GamificationResponse("GAMIFICATION", 100, 2.2, BigDecimal.ONE, BigDecimal.ZERO, 1,
            BigDecimal.TEN,
            PromotionsResponse.Status.ACTIVE, true)
    val referralResponse =
        ReferralResponse("REFERRAL", 99, BigDecimal(2.2), 3, true, 2, "EUR", "€", false, "link",
            BigDecimal.ONE, BigDecimal.ZERO, ReferralResponse.UserStatus.REDEEMED, BigDecimal.ZERO,
            PromotionsResponse.Status.ACTIVE, BigDecimal.ONE)

    api.userStatusResponse =
        Single.just(UserStatusResponse(
            listOf(userStatsGamification, referralResponse), WalletOrigin.APTOIDE))
    val testObserver = gamification.getUserStats(WALLET)
        .test()
    testObserver.assertValue(
        GamificationStats(GamificationStats.Status.OK, 1, BigDecimal.TEN, 2.2, BigDecimal.ONE,
            BigDecimal.ZERO, isActive = true))
  }

  @Test
  fun getUserStatsNoNetworkTest() {
    api.userStatusResponse = Single.error(UnknownHostException())
    local.userStatusResponse = Single.just(emptyList())
    local.walletOriginResponse = Single.just(WalletOrigin.APTOIDE)
    val testObserver = gamification.getUserStats(WALLET)
        .test()
    testObserver.assertValue(GamificationStats(GamificationStats.Status.NO_NETWORK))
  }

  @Test
  fun getLevels() {
    api.levelsResponse = Single.just(
        LevelsResponse(listOf(Level(BigDecimal.ONE, 2.0, 1), Level(BigDecimal.TEN, 20.0, 2)),
            LevelsResponse.Status.ACTIVE, date))
    val testObserver = gamification.getLevels(WALLET)
        .test()
    testObserver.assertValue(
        Levels(Levels.Status.OK,
            listOf(Levels.Level(BigDecimal.ONE, 2.0, 1), Levels.Level(BigDecimal.TEN, 20.0, 2)),
            true, date))
  }

  @Test
  fun getLevelsNoNetwork() {
    api.levelsResponse = Single.error(UnknownHostException())
    local.levelsResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getLevels(WALLET)
        .test()
    testObserver.assertValue(Levels(Levels.Status.NO_NETWORK))
  }

  @Test
  fun getBonus() {
    api.bonusResponse = Single.just(
        ForecastBonusResponse(BigDecimal.ONE, 0, ForecastBonusResponse.Status.ACTIVE))
    val testObserver = gamification.getEarningBonus(WALLET, PACKAGE_NAME, BigDecimal.ONE)
        .test()
    testObserver.assertValue(ForecastBonus(ForecastBonus.Status.ACTIVE, BigDecimal.ONE))
  }

  @Test
  fun getBonusNoNetwork() {
    api.bonusResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getEarningBonus(WALLET, PACKAGE_NAME, BigDecimal.ONE)
        .test()
    testObserver.assertValue(ForecastBonus(ForecastBonus.Status.NO_NETWORK))
  }

  @Test
  fun hasNewLevelNoNewLevel() {
    val userStatsGamification =
        GamificationResponse(GAMIFICATION_ID, 100, 2.2, BigDecimal.ONE, BigDecimal.ZERO, 0,
            BigDecimal.TEN,
            PromotionsResponse.Status.ACTIVE, true)
    val referralResponse =
        ReferralResponse(REFERRAL_ID, 99, BigDecimal(2.2), 3, true, 2, "EUR", "€", false, "link",
            BigDecimal.ONE,
            BigDecimal.ZERO, ReferralResponse.UserStatus.REDEEMED, BigDecimal.ZERO,
            PromotionsResponse.Status.ACTIVE,
            BigDecimal.ONE)

    api.userStatusResponse =
        Single.just(UserStatusResponse(listOf(userStatsGamification, referralResponse),
            WalletOrigin.APTOIDE))
    local.lastShownLevelResponse = Single.just(0)
    val test = gamification.hasNewLevel(WALLET, GamificationScreen.MY_LEVEL.toString())
        .test()
    test.assertValue(false)
        .assertNoErrors()
        .assertComplete()
  }

  @Test
  fun hasNewLevelNewLevel() {
    val userStatsGamification =
        GamificationResponse(GAMIFICATION_ID, 100, 2.2, BigDecimal.ONE, BigDecimal.ZERO, 0,
            BigDecimal.TEN,
            PromotionsResponse.Status.ACTIVE, true)
    val referralResponse =
        ReferralResponse(REFERRAL_ID, 99, BigDecimal(2.2), 3, true, 2, "EUR", "€", false, "link",
            BigDecimal.ONE,
            BigDecimal.ZERO, ReferralResponse.UserStatus.REDEEMED, BigDecimal.ZERO,
            PromotionsResponse.Status.ACTIVE,
            BigDecimal.ONE)

    api.userStatusResponse =
        Single.just(UserStatusResponse(listOf(userStatsGamification, referralResponse),
            WalletOrigin.APTOIDE))
    local.lastShownLevelResponse = Single.just(-1)
    val test = gamification.hasNewLevel(WALLET, GamificationScreen.MY_LEVEL.toString())
        .test()
    test.assertValue(true)
        .assertNoErrors()
        .assertComplete()
  }

  @Test
  fun hasNewLevelNetworkError() {
    api.userStatusResponse = Single.just(UserStatusResponse(Status.NO_NETWORK))
    local.lastShownLevelResponse = Single.just(-1)
    val test = gamification.hasNewLevel(WALLET, GamificationScreen.MY_LEVEL.toString())
        .test()
    test.assertValue(false)
        .assertNoErrors()
        .assertComplete()
  }

  @Test
  fun levelShown() {
    val shownLevel = 1
    val test = gamification.levelShown(WALLET, shownLevel, GamificationScreen.MY_LEVEL.toString())
        .test()
    test.assertNoErrors()
        .assertComplete()
    local.lastShownLevelResponse!!.test()
        .assertValue(shownLevel)
    Assert.assertEquals("the updated wallet was not the expected one", WALLET, local.getWallet())
  }

}