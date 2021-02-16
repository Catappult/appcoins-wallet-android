package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.entity.*
import com.appcoins.wallet.gamification.repository.entity.WalletOrigin
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class PromotionsRepositoryTest {

  private lateinit var promotionsRepository: PromotionsRepository

  private companion object {
    private const val TEST_WALLET_ADDRESS = "0x00000"
    private const val TEST_START_DATE: Long = 1000
    private const val TEST_END_DATE: Long = 11111000000000000
    private const val TEST_WRONG_END_DATE: Long = 0
    private const val TEST_GAMIFICATION_LEVEL = 1
  }

  @Mock
  lateinit var api: GamificationApi

  @Mock
  lateinit var local: UserStatsLocalData

  private val testWalletOrigin = WalletOrigin.APTOIDE
  private lateinit var gamificationResponse: GamificationResponse
  private lateinit var genericProgressResponse: GenericResponse
  private lateinit var genericActiveResponse: GenericResponse
  private lateinit var wrongResponse: GenericResponse
  private lateinit var promotions: List<PromotionsResponse>

  @Before
  fun setup() {
    gamificationResponse =
        GamificationResponse(PromotionsInteractor.GAMIFICATION_ID, 100, 20.0, BigDecimal.ONE,
            BigDecimal.ONE, TEST_GAMIFICATION_LEVEL, BigDecimal.TEN,
            PromotionsResponse.Status.ACTIVE, false)
    genericProgressResponse =
        GenericResponse("id1", 0, BigDecimal(50), "description", TEST_END_DATE, null, null,
            BigDecimal(100), TEST_START_DATE, "Progress", "PROGRESS", null)
    genericActiveResponse =
        GenericResponse("id2", 0, null, "description", TEST_END_DATE, null, null, null,
            TEST_START_DATE, "default", "DEFAULT", null)
    wrongResponse =
        GenericResponse("id3", 32, null, "description", TEST_WRONG_END_DATE, null,
            PromotionsInteractor.GAMIFICATION_ID, null,
            TEST_START_DATE, "link", "DEFAULT", "any")
    promotions =
        listOf(gamificationResponse, genericProgressResponse, genericActiveResponse, wrongResponse)
    promotionsRepository = BdsPromotionsRepository(api, local)
    Mockito.`when`(api.getUserStats(TEST_WALLET_ADDRESS, Locale.getDefault().language))
        .thenReturn(Single.just(UserStatusResponse(promotions, testWalletOrigin)))
  }

  @Test
  fun getUserStatusTest() {
    mockUserStatsCalls()
    Mockito.`when`(local.setGamificationLevel(TEST_GAMIFICATION_LEVEL))
        .thenReturn(Completable.complete())

    val userStats = promotionsRepository.getUserStatus(TEST_WALLET_ADDRESS)
        .blockingGet()

    Mockito.verify(local)
        .setGamificationLevel(TEST_GAMIFICATION_LEVEL)
    //Test the filter by date for perks with end date less than the actual date
    Assert.assertEquals(userStats.promotions.size, promotions.size - 1)
    assertUserStatsCalls()
  }

  @Test
  fun getUserStatusErrorTest() {
    Mockito.`when`(local.getPromotions())
        .thenReturn(Single.just(promotions))
    Mockito.`when`(local.retrieveWalletOrigin(TEST_WALLET_ADDRESS))
        .thenReturn(Single.just(testWalletOrigin))
    Mockito.`when`(local.setGamificationLevel(TEST_GAMIFICATION_LEVEL))
        .thenReturn(Completable.complete())
    Mockito.`when`(api.getUserStats(TEST_WALLET_ADDRESS, Locale.getDefault().language))
        .thenReturn(Single.error(Throwable("Generic")))

    val userStats = promotionsRepository.getUserStatus(TEST_WALLET_ADDRESS)
        .blockingGet()

    Mockito.verify(local)
        .setGamificationLevel(TEST_GAMIFICATION_LEVEL)
    Assert.assertEquals(userStats.promotions.size, 4)
    Mockito.verify(local)
        .getPromotions()
    Mockito.verify(local)
        .retrieveWalletOrigin(TEST_WALLET_ADDRESS)
  }

  @Test
  fun getUserStatusNoDatabaseAndApiErrorTest() {
    Mockito.`when`(local.getPromotions())
        .thenReturn(Single.just(emptyList()))
    Mockito.`when`(local.retrieveWalletOrigin(TEST_WALLET_ADDRESS))
        .thenReturn(Single.just(testWalletOrigin))

    Mockito.`when`(api.getUserStats(TEST_WALLET_ADDRESS, Locale.getDefault().language))
        .thenReturn(Single.error(Throwable("Generic")))

    val userStats = promotionsRepository.getUserStatus(TEST_WALLET_ADDRESS)
        .blockingGet()

    Assert.assertEquals(userStats.promotions.size, 0)
    Assert.assertEquals(userStats.error, Status.UNKNOWN_ERROR)
    Mockito.verify(local)
        .getPromotions()
    Mockito.verify(local)
        .retrieveWalletOrigin(TEST_WALLET_ADDRESS)
    Mockito.verifyNoMoreInteractions(local)
  }

  @Test
  fun getLastShownLevelTest() {
    Mockito.`when`(
        local.getLastShownLevel(TEST_WALLET_ADDRESS, GamificationContext.SCREEN_PROMOTIONS))
        .thenReturn(Single.just(1))

    val lastShownLevel = promotionsRepository.getLastShownLevel(TEST_WALLET_ADDRESS,
        GamificationContext.SCREEN_PROMOTIONS)
        .blockingGet()

    Assert.assertEquals(1, lastShownLevel)
  }

  @Test
  fun shownLevelTest() {
    promotionsRepository.shownLevel(TEST_WALLET_ADDRESS, TEST_GAMIFICATION_LEVEL,
        GamificationContext.SCREEN_PROMOTIONS)
    Mockito.verify(local)
        .saveShownLevel(TEST_WALLET_ADDRESS, TEST_GAMIFICATION_LEVEL,
            GamificationContext.SCREEN_PROMOTIONS)
  }

  @Test
  fun getSeenGenericPromotionTest() {
    promotionsRepository.getSeenGenericPromotion(Mockito.anyString(), Mockito.anyString())
    Mockito.verify(local)
        .getSeenGenericPromotion(Mockito.anyString(), Mockito.anyString())
  }

  @Test
  fun setSeenGenericPromotionTest() {
    promotionsRepository.setSeenGenericPromotion(Mockito.anyString(), Mockito.anyString())
    Mockito.verify(local)
        .setSeenGenericPromotion(Mockito.anyString(), Mockito.anyString())
  }

  @Test
  fun getForecastBonusActiveTest() {
    Mockito.`when`(
        api.getForecastBonus(TEST_WALLET_ADDRESS, "packageName", BigDecimal.TEN, "APPC"))
        .thenReturn(Single.just(ForecastBonusResponse(BigDecimal.ONE, TEST_GAMIFICATION_LEVEL,
            ForecastBonusResponse.Status.ACTIVE)))
    val testForecastBonus = ForecastBonus(ForecastBonus.Status.ACTIVE, BigDecimal.ONE, "")
    val forecastBonus =
        promotionsRepository.getForecastBonus(TEST_WALLET_ADDRESS, "packageName", BigDecimal.TEN)
            .blockingGet()

    Assert.assertEquals(forecastBonus, testForecastBonus)
  }

  @Test
  fun getForecastBonusInactiveTest() {
    Mockito.`when`(
        api.getForecastBonus(TEST_WALLET_ADDRESS, "packageName", BigDecimal.TEN, "APPC"))
        .thenReturn(Single.just(ForecastBonusResponse(BigDecimal.ONE, TEST_GAMIFICATION_LEVEL,
            ForecastBonusResponse.Status.INACTIVE)))
    val testForecastBonus = ForecastBonus(ForecastBonus.Status.INACTIVE, BigDecimal.ZERO, "")
    val forecastBonus =
        promotionsRepository.getForecastBonus(TEST_WALLET_ADDRESS, "packageName", BigDecimal.TEN)
            .blockingGet()

    Assert.assertEquals(forecastBonus, testForecastBonus)
  }

  @Test
  fun getGamificationStatsTest() {
    mockUserStatsCalls()

    Mockito.`when`(local.setGamificationLevel(TEST_GAMIFICATION_LEVEL))
        .thenReturn(Completable.complete())

    val testGamificationStats =
        GamificationStats(GamificationStats.Status.OK, TEST_GAMIFICATION_LEVEL,
            gamificationResponse.nextLevelAmount, gamificationResponse.bonus,
            gamificationResponse.totalSpend, gamificationResponse.totalEarned, true)
    val gamificationStats = promotionsRepository.getGamificationStats(TEST_WALLET_ADDRESS)
        .blockingGet()

    Mockito.verify(local)
        .setGamificationLevel(TEST_GAMIFICATION_LEVEL)
    Assert.assertEquals(testGamificationStats, gamificationStats)
    assertUserStatsCalls()
  }

  @Test
  fun getLevelsTest() {
    val response = LevelsResponse(listOf(Level(BigDecimal.TEN, 20.0, TEST_GAMIFICATION_LEVEL)),
        LevelsResponse.Status.ACTIVE, null)
    Mockito.`when`(api.getLevels(TEST_WALLET_ADDRESS))
        .thenReturn(Single.just(response))
    Mockito.`when`(local.deleteLevels())
        .thenReturn(Completable.complete())
    Mockito.`when`(local.insertLevels(response))
        .thenReturn(Completable.complete())

    val testLevels = Levels(Levels.Status.OK,
        listOf(Levels.Level(BigDecimal.TEN, 20.0, TEST_GAMIFICATION_LEVEL)), true, null)
    val levels = promotionsRepository.getLevels(TEST_WALLET_ADDRESS)
        .blockingGet()

    Assert.assertEquals(testLevels, levels)
    Mockito.verify(local)
        .deleteLevels()
    Mockito.verify(local)
        .insertLevels(response)
  }

  @Test
  fun getLevelsApiErrorTest() {
    val response = LevelsResponse(listOf(Level(BigDecimal.TEN, 20.0, TEST_GAMIFICATION_LEVEL)),
        LevelsResponse.Status.ACTIVE, null)
    Mockito.`when`(api.getLevels(TEST_WALLET_ADDRESS))
        .thenReturn(Single.error(Throwable("Any")))
    Mockito.`when`(local.getLevels())
        .thenReturn(Single.just(response))

    val testLevels = Levels(Levels.Status.OK,
        listOf(Levels.Level(BigDecimal.TEN, 20.0, TEST_GAMIFICATION_LEVEL)), true, null)
    val levels = promotionsRepository.getLevels(TEST_WALLET_ADDRESS)
        .blockingGet()

    Assert.assertEquals(testLevels, levels)
  }

  @Test
  fun getLevelsDbEmptyAndApiErrorTest() {
    Mockito.`when`(api.getLevels(TEST_WALLET_ADDRESS))
        .thenReturn(Single.error(Throwable("AnyApi")))
    Mockito.`when`(local.getLevels())
        .thenReturn(Single.error(Throwable("AnyDb")))

    val testLevels = Levels(Levels.Status.UNKNOWN_ERROR, emptyList(), false, null)
    val levels = promotionsRepository.getLevels(TEST_WALLET_ADDRESS)
        .blockingGet()

    Assert.assertEquals(testLevels, levels)
  }

  private fun assertUserStatsCalls() {
    Mockito.verify(local)
        .deletePromotions()
    Mockito.verify(local)
        .insertPromotions(Mockito.anyList())
    Mockito.verify(local)
        .insertWalletOrigin(TEST_WALLET_ADDRESS, testWalletOrigin)
  }

  private fun mockUserStatsCalls() {
    Mockito.`when`(local.deletePromotions())
        .thenReturn(Completable.complete())
    Mockito.`when`(local.insertPromotions(Mockito.anyList()))
        .thenReturn(Completable.complete())
    Mockito.`when`(local.insertWalletOrigin(TEST_WALLET_ADDRESS, testWalletOrigin))
        .thenReturn(Completable.complete())
  }
}