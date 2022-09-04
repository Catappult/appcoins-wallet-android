package com.asfoundation.wallet.app_start

import app.cash.turbine.testIn
import com.asfoundation.wallet.gherkin.coScenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.TimeUnit

/**
 * AS a Wallet Developer,
 * I WANT to track modes of app start,
 * FOR analysing newcomers and altering the first screen
 *
 * Since it is impossible to know if app data was cleared or not we assume that if it is not a fresh
 * install then this is not the first run.
 * So the first run event will occur only if app was not updated yet and run count is 0
 */

@ExperimentalCoroutinesApi
internal class AppStartUseCaseTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("testDataProvider")
  fun `Listen before job started`(data: TestData) = coScenario { scope ->

    m Given "use case with the given repository mock"
    val useCase = AppStartUseCase(data.givenData, StandardTestDispatcher(scope.testScheduler))
    m And "subscribed for modes"
    val modes = useCase.startModes.testIn(scope)

    m When "app started"
    useCase.registerAppStart()
    m And "job finished"
    scope.advanceUntilIdle()

    m Then "run mode = expected mode from ThenData"
    assertEquals(data.thenData.mode, modes.awaitItem())
    m And "runs count = expected runs count from ThenData"
    assertEquals(data.thenData.runCount, data.givenData.runCount)
    m But "no more modes received"
    modes.cancel()
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testDataProvider")
  fun `Listen before job finished`(data: TestData) = coScenario { scope ->

    m Given "use case with the given repository mock"
    val useCase = AppStartUseCase(data.givenData, StandardTestDispatcher(scope.testScheduler))

    m When "app started"
    useCase.registerAppStart()
    m And "job running"
    scope.advanceTimeBy(50)
    m And "subscribed for modes"
    val modes = useCase.startModes.testIn(scope)
    m And "job finished"
    scope.advanceUntilIdle()

    m Then "run mode = expected mode from ThenData"
    assertEquals(data.thenData.mode, modes.awaitItem())
    m And "runs count = expected runs count from ThenData"
    assertEquals(data.thenData.runCount, data.givenData.runCount)
    m But "no more modes received"
    modes.cancel()
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testDataProvider")
  fun `Listen after job finished`(data: TestData) = coScenario { scope ->

    m Given "use case with the given repository mock"
    val useCase = AppStartUseCase(data.givenData, StandardTestDispatcher(scope.testScheduler))

    m When "app started"
    useCase.registerAppStart()
    m And "job finished"
    scope.advanceUntilIdle()
    m And "subscribed for modes"
    val modes = useCase.startModes.testIn(scope)

    m Then "run mode = expected mode from ThenData"
    assertEquals(data.thenData.mode, modes.awaitItem())
    m And "runs count = expected runs count from ThenData"
    assertEquals(data.thenData.runCount, data.givenData.runCount)
    m But "no more modes received"
    modes.cancel()
  }

  companion object {
    private const val UTM_OSP =
      "utm_source=aptoide&utm_medium=com.igg.android.lordsmobile&utm_term=osp&utm_content=13204"
    private const val UTM_SDK =
      "utm_source=aptoide&utm_medium=com.igg.android.lordsmobile&utm_term=sdk&utm_content=13204"
    private const val UTM_WRONG_TERM =
      "utm_source=aptoide&utm_medium=com.igg.android.lordsmobile&utm_term=something&utm_content=13204"
    private const val UTM_NO_MEDIUM = "utm_source=aptoide&utm_term=osp&utm_content=13204"
    private const val UTM_INVALID =
      "���ﾟ����肄ｽ碣��鱠螯���ﾟ�裝鵄�ｽ胥�ｮ鱸邂碚蔗�鱠ｮ���蔘��粳�螯���ﾟ�褪�ｽ���ｦ���ﾟ胥��褓�ｽｱｳｲｰｴ"

    @JvmStatic
    fun testDataProvider(): List<TestData> = listOf(
      TestData(
        scenario = "App started -> First",
        givenData = RepositoryMock(),
        thenData = ThenData()
      ),
      TestData(
        scenario = "App started with OSP UTM -> FirstUtm",
        givenData = RepositoryMock(referrer = UTM_OSP),
        thenData = ThenData(
          mode = StartMode.FirstUtm(
            sku = "13204",
            source = "aptoide",
            packageName = "com.igg.android.lordsmobile",
            integrationFlow = "osp"
          )
        )
      ),
      TestData(
        scenario = "App started with SDK UTM -> FirstUtm",
        givenData = RepositoryMock(referrer = UTM_SDK),
        thenData = ThenData(
          mode = StartMode.FirstUtm(
            sku = "13204",
            source = "aptoide",
            packageName = "com.igg.android.lordsmobile",
            integrationFlow = "sdk"
          ),
        )
      ),
      TestData(
        scenario = "App started with wrong term UTM -> First",
        givenData = RepositoryMock(referrer = UTM_WRONG_TERM),
        thenData = ThenData()
      ),
      TestData(
        scenario = "App started with no medium UTM -> First",
        givenData = RepositoryMock(referrer = UTM_NO_MEDIUM),
        thenData = ThenData()
      ),
      TestData(
        scenario = "App started with invalid UTM -> First",
        givenData = RepositoryMock(referrer = UTM_INVALID),
        thenData = ThenData()
      ),
      TestData(
        scenario = "App started after update -> Subsequent",
        givenData = RepositoryMock(updatedAfter = 5.days()),
        thenData = ThenData(mode = StartMode.Subsequent)
      ),
      TestData(
        scenario = "App started after update with UTM -> Subsequent",
        givenData = RepositoryMock(updatedAfter = 5.days(), referrer = UTM_OSP),
        thenData = ThenData(mode = StartMode.Subsequent)
      ),
      TestData(
        scenario = "App started after update with invalid UTM -> Subsequent",
        givenData = RepositoryMock(updatedAfter = 5.days(), referrer = UTM_INVALID),
        thenData = ThenData(mode = StartMode.Subsequent)
      ),
      TestData(
        scenario = "App started for the second time -> Subsequent",
        givenData = RepositoryMock(runCount = 1),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time with UTM -> Subsequent",
        givenData = RepositoryMock(runCount = 1, referrer = UTM_OSP),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time with invalid UTM -> Subsequent",
        givenData = RepositoryMock(runCount = 1, referrer = UTM_INVALID),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time after update -> Subsequent",
        givenData = RepositoryMock(runCount = 1, updatedAfter = 5.days()),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time after update with UTM -> Subsequent",
        givenData = RepositoryMock(runCount = 1, updatedAfter = 5.days(), referrer = UTM_OSP),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time after update with invalid UTM -> Subsequent",
        givenData = RepositoryMock(runCount = 1, updatedAfter = 5.days(), referrer = UTM_INVALID),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      )
    )
  }
}

internal data class TestData(
  val scenario: String,
  val givenData: RepositoryMock,
  val thenData: ThenData,
) {
  override fun toString() = scenario
}

internal data class ThenData(
  val mode: StartMode = StartMode.First,
  val runCount: Int = 1,
)

private fun Number.days() = TimeUnit.DAYS.toMillis(this.toLong())
private fun Number.daysAgo() = System.currentTimeMillis() - this.days()

internal class RepositoryMock(
  var runCount: Int = 0,
  private val InstallAgo: Long = 15.daysAgo(),
  private val updatedAfter: Long = 0,
  private val referrer: String? = null,
) : AppStartRepository {

  override suspend fun getRunCount(): Int {
    delay(100)
    return runCount
  }

  override suspend fun saveRunCount(count: Int) {
    delay(200)
    this@RepositoryMock.runCount = count
  }

  override suspend fun getFirstInstallTime(): Long {
    delay(100)
    return InstallAgo
  }

  override suspend fun getLastUpdateTime(): Long {
    delay(100)
    return InstallAgo + updatedAfter
  }

  override suspend fun getReferrerUrl(): String? {
    delay(800)
    return referrer
  }
}
