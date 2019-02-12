import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.GamificationApiTest
import com.appcoins.wallet.gamification.GamificationLocalDataTest
import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.entity.Level
import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.net.UnknownHostException

class GamificationTest {
  private lateinit var gamification: Gamification
  private val api = GamificationApiTest()
  private val local = GamificationLocalDataTest()
  private val wallet = "wallet1"
  private val packageName = "packageName"

  @Before
  @Throws(Exception::class)
  fun setUp() {
    gamification = Gamification(BdsGamificationRepository(api, local))
  }

  @Test
  fun getUserStatsTest() {
    api.userStatusResponse = Single.just(
        UserStatusResponse(2.2, BigDecimal.ONE, BigDecimal.ZERO, 1, BigDecimal.TEN,
            UserStatusResponse.Status.ACTIVE))
    val testObserver = gamification.getUserStatus(wallet).test()
    testObserver.assertValue(
        UserStats(UserStats.Status.OK, 1, BigDecimal.TEN, 2.2, BigDecimal.ONE, BigDecimal.ZERO,
            true))
  }

  @Test
  fun getUserStatsNoNetworkTest() {
    api.userStatusResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getUserStatus(wallet).test()
    testObserver.assertValue(UserStats(UserStats.Status.NO_NETWORK))
  }

  @Test
  fun getLevels() {
    api.levelsResponse = Single.just(
        LevelsResponse(listOf(Level(BigDecimal.ONE, 2.0, 1), Level(BigDecimal.TEN, 20.0, 2)),
            LevelsResponse.Status.ACTIVE))
    val testObserver = gamification.getLevels().test()
    testObserver.assertValue(
        Levels(Levels.Status.OK,
            listOf(Levels.Level(BigDecimal.ONE, 2.0, 1), Levels.Level(BigDecimal.TEN, 20.0, 2)),
            true))
  }

  @Test
  fun getLevelsNoNetwork() {
    api.levelsResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getLevels().test()
    testObserver.assertValue(Levels(Levels.Status.NO_NETWORK))
  }

  @Test
  fun getBonus() {
    api.bonusResponse = Single.just(
        ForecastBonusResponse(BigDecimal.ONE, 0, ForecastBonusResponse.Status.ACTIVE))
    val testObserver = gamification.getEarningBonus(wallet, packageName, BigDecimal.ONE).test()
    testObserver.assertValue(ForecastBonus(ForecastBonus.Status.ACTIVE, BigDecimal.ONE))
  }

  @Test
  fun getBonusNoNetwork() {
    api.bonusResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getEarningBonus(wallet, packageName, BigDecimal.ONE).test()
    testObserver.assertValue(ForecastBonus(ForecastBonus.Status.NO_NETWORK))
  }

  @Test
  fun hasNewLevelNoNewLevel() {
    api.userStatusResponse = Single.just(
        UserStatusResponse(2.2, BigDecimal.ONE, BigDecimal.ZERO, 0, BigDecimal.TEN,
            UserStatusResponse.Status.ACTIVE))
    local.lastShownLevelResponse = Single.just(0)
    val test = gamification.hasNewLevel(wallet).test()
    test.assertValue(false).assertNoErrors().assertComplete()
  }

  @Test
  fun hasNewLevelNewLevel() {
    api.userStatusResponse = Single.just(
        UserStatusResponse(2.2, BigDecimal.ONE, BigDecimal.ZERO, 0, BigDecimal.TEN,
            UserStatusResponse.Status.ACTIVE))
    local.lastShownLevelResponse = Single.just(-1)
    val test = gamification.hasNewLevel(wallet).test()
    test.assertValue(true).assertNoErrors().assertComplete()
  }

  @Test
  fun hasNewLevelNetworkError() {
    api.userStatusResponse = Single.error(UnknownHostException())
    local.lastShownLevelResponse = Single.just(-1)
    val test = gamification.hasNewLevel(wallet).test()
    test.assertValue(false).assertNoErrors().assertComplete()
  }

  @Test
  fun levelShown() {
    val shownLevel = 1
    val test = gamification.levelShown(wallet, shownLevel).test()
    test.assertNoErrors().assertComplete()
    local.lastShownLevelResponse!!.test().assertValue(shownLevel)
    Assert.assertEquals("the updated wallet was not the expected one", wallet, local.getWallet())
  }
}