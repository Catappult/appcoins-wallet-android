import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.GamificationApiTest
import com.appcoins.wallet.gamification.repository.BdsGamificationRepository
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.appcoins.wallet.gamification.repository.entity.Level
import com.appcoins.wallet.gamification.repository.entity.LevelsResponse
import com.appcoins.wallet.gamification.repository.entity.UserStatusResponse
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.net.UnknownHostException

class GamificationTest {
  private lateinit var gamification: Gamification
  private val api = GamificationApiTest()
  private val wallet = "wallet1"

  @Before
  @Throws(Exception::class)
  fun setUp() {
    gamification = Gamification(BdsGamificationRepository(api))
  }

  @Test
  fun getUserStatsTest() {
    api.userStatusResponse = Single.just(UserStatusResponse(2.2, BigDecimal.ONE, BigDecimal.ZERO, 1, BigDecimal.TEN))
    val testObserver = gamification.getUserStatus(wallet).test()
    testObserver.assertValue(
        UserStats(UserStats.Status.OK, 1, BigDecimal.TEN, 2.2, BigDecimal.ONE, BigDecimal.ZERO))
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
        LevelsResponse(listOf(Level(BigDecimal.ONE, 2.0, 1), Level(BigDecimal.TEN, 20.0, 2))))
    val testObserver = gamification.getLevels().test()
    testObserver.assertValue(
        Levels(Levels.Status.OK,
            listOf(Levels.Level(BigDecimal.ONE, 2.0, 1), Levels.Level(BigDecimal.TEN, 20.0, 2))))
  }

  @Test
  fun getLevelsNoNetwork() {
    api.levelsResponse = Single.error(UnknownHostException())
    val testObserver = gamification.getLevels().test()
    testObserver.assertValue(Levels(Levels.Status.NO_NETWORK))
  }
}