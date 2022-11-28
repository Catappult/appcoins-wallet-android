package com.asfoundation.wallet.app_start

import app.cash.turbine.testIn
import com.asfoundation.wallet.gherkin.coScenario
import com.asfoundation.wallet.onboarding.use_cases.PendingPurchaseFlowUseCase
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

    m Given "use case with the given repository, GPInstallUseCase and PendingPurchaseFlowUseCase mocks"
    val useCase = AppStartUseCase(
      data.givenData.repository,
      data.givenData.GPInstallUseCase,
      data.givenData.PendingPurchaseFlowUseCase,
      StandardTestDispatcher(scope.testScheduler)
    )
    m And "subscribed for modes"
    val modes = useCase.startModes.testIn(scope)

    m When "app started"
    useCase.registerAppStart()
    m And "job finished"
    scope.advanceUntilIdle()

    m Then "run mode = expected mode from ThenData"
    assertEquals(data.thenData.mode, modes.awaitItem())
    m And "runs count = expected runs count from ThenData"
    assertEquals(data.thenData.runCount, data.givenData.repository.runCount)
    m But "no more modes received"
    modes.cancel()
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testDataProvider")
  fun `Listen before job finished`(data: TestData) = coScenario { scope ->

    m Given "use case with the given repository, GPInstallUseCase and PendingPurchaseFlowUseCase mocks"
    val useCase = AppStartUseCase(
      data.givenData.repository,
      data.givenData.GPInstallUseCase,
      data.givenData.PendingPurchaseFlowUseCase,
      StandardTestDispatcher(scope.testScheduler)
    )

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
    assertEquals(data.thenData.runCount, data.givenData.repository.runCount)
    m But "no more modes received"
    modes.cancel()
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testDataProvider")
  fun `Listen after job finished`(data: TestData) = coScenario { scope ->

    m Given "use case with the given repository, GPInstallUseCase and PendingPurchaseFlowUseCase mocks"
    val useCase = AppStartUseCase(
      data.givenData.repository,
      data.givenData.GPInstallUseCase,
      data.givenData.PendingPurchaseFlowUseCase,
      StandardTestDispatcher(scope.testScheduler)
    )

    m When "app started"
    useCase.registerAppStart()
    m And "job finished"
    scope.advanceUntilIdle()
    m And "subscribed for modes"
    val modes = useCase.startModes.testIn(scope)

    m Then "run mode = expected mode from ThenData"
    assertEquals(data.thenData.mode, modes.awaitItem())
    m And "runs count = expected runs count from ThenData"
    assertEquals(data.thenData.runCount, data.givenData.repository.runCount)
    m But "no more modes received"
    modes.cancel()
  }

  companion object {
    private fun gpInstall(
      sku: String = "13204",
      source: String = "aptoide",
      packageName: String = "com.igg.android.lordsmobile",
      integration: String = "osp"
    ) = StartMode.GPInstall(sku, source, packageName, integration)

    private fun pendingPurchase(
      integrationFlow: String = "osp",
      sku: String = "13204",
      packageName: String = "com.igg.android.lordsmobile",
      callbackUrl: String = "",
      currency: String = "USD",
      orderReference: String = "",
      value: Double = 1.5,
      signature: String = ""
    ) = StartMode.PendingPurchaseFlow(
      integrationFlow,
      sku,
      packageName,
      callbackUrl,
      currency,
      orderReference,
      value,
      signature
    )

    @JvmStatic
    fun testDataProvider(): List<TestData> = listOf(
      TestData(
        scenario = "App started -> First",
        givenData = GivenData(),
        thenData = ThenData()
      ),
      TestData(
        scenario = "App started with GP Install -> GPInstall",
        givenData = GivenData(GPInstallUseCase = GPInstallUseCaseMock(gpInstall())),
        thenData = ThenData(mode = gpInstall())
      ),
      TestData(
        scenario = "App started with Pending Purchase -> PendingPurchaseFlow",
        givenData = GivenData(
          PendingPurchaseFlowUseCase = PendingPurchaseFlowUseCaseMock(pendingPurchase())
        ),
        thenData = ThenData(mode = pendingPurchase())
      ),
      TestData(
        scenario = "App started after update -> Subsequent",
        givenData = GivenData(repository = AppStartRepositoryMock(updatedAfter = 5.days())),
        thenData = ThenData(mode = StartMode.Subsequent)
      ),
      TestData(
        scenario = "App started after update with GP Install -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(updatedAfter = 5.days()),
          GPInstallUseCase = GPInstallUseCaseMock(gpInstall())
        ),
        thenData = ThenData(mode = StartMode.Subsequent)
      ),
      TestData(
        scenario = "App started after update with Pending Purchase -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(updatedAfter = 5.days()),
          PendingPurchaseFlowUseCase = PendingPurchaseFlowUseCaseMock(pendingPurchase())
        ),
        thenData = ThenData(mode = StartMode.Subsequent)
      ),
      TestData(
        scenario = "App started after update with Pending Purchase and GP Install -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(updatedAfter = 5.days()),
          PendingPurchaseFlowUseCase = PendingPurchaseFlowUseCaseMock(pendingPurchase()),
          GPInstallUseCase = GPInstallUseCaseMock(gpInstall())
        ),
        thenData = ThenData(mode = StartMode.Subsequent)
      ),
      TestData(
        scenario = "App started for the second time -> Subsequent",
        givenData = GivenData(repository = AppStartRepositoryMock(runCount = 1)),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time with GP Install -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(runCount = 1),
          GPInstallUseCase = GPInstallUseCaseMock(gpInstall())
        ),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time with Pending Purchase -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(runCount = 1),
          PendingPurchaseFlowUseCase = PendingPurchaseFlowUseCaseMock(pendingPurchase())
        ),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time with Pending Purchase and GP Install -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(runCount = 1),
          PendingPurchaseFlowUseCase = PendingPurchaseFlowUseCaseMock(pendingPurchase()),
          GPInstallUseCase = GPInstallUseCaseMock(gpInstall())
        ),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time after update -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(runCount = 1, updatedAfter = 5.days())
        ),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time after update with Pending Purchase -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(runCount = 1, updatedAfter = 5.days()),
          PendingPurchaseFlowUseCase = PendingPurchaseFlowUseCaseMock(pendingPurchase())
        ),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time after update with GP Install -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(runCount = 1, updatedAfter = 5.days()),
          GPInstallUseCase = GPInstallUseCaseMock(gpInstall())
        ),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      ),
      TestData(
        scenario = "App started for the second time after update with Pending Purchase and GP Install -> Subsequent",
        givenData = GivenData(
          repository = AppStartRepositoryMock(runCount = 1, updatedAfter = 5.days()),
          PendingPurchaseFlowUseCase = PendingPurchaseFlowUseCaseMock(pendingPurchase()),
          GPInstallUseCase = GPInstallUseCaseMock(gpInstall())
        ),
        thenData = ThenData(mode = StartMode.Subsequent, runCount = 2)
      )
    )
  }

  internal data class TestData(
    val scenario: String,
    val givenData: GivenData,
    val thenData: ThenData,
  ) {
    override fun toString() = scenario
  }

  internal data class GivenData(
    val repository: AppStartRepositoryMock = AppStartRepositoryMock(),
    val GPInstallUseCase: GPInstallUseCase = GPInstallUseCaseMock(),
    val PendingPurchaseFlowUseCase: PendingPurchaseFlowUseCase = PendingPurchaseFlowUseCaseMock(),
  )

  internal data class ThenData(
    val mode: StartMode = StartMode.Regular,
    val runCount: Int = 1,
  )
}

private fun Number.days() = TimeUnit.DAYS.toMillis(this.toLong())
private fun Number.daysAgo() = System.currentTimeMillis() - this.days()

internal class AppStartRepositoryMock(
  var runCount: Int = 0,
  private val InstallAgo: Long = 15.daysAgo(),
  private val updatedAfter: Long = 0,
) : AppStartRepository {
  override suspend fun getRunCount(): Int {
    delay(100)
    return runCount
  }

  override suspend fun saveRunCount(count: Int) {
    delay(200)
    this.runCount = count
  }

  override suspend fun getFirstInstallTime(): Long {
    delay(100)
    return InstallAgo
  }

  override suspend fun getLastUpdateTime(): Long {
    delay(100)
    return InstallAgo + updatedAfter
  }
}

class GPInstallUseCaseMock(
  private val GPInstall: StartMode.GPInstall? = null
) : GPInstallUseCase {
  override suspend operator fun invoke(): StartMode.GPInstall? {
    delay(800)
    return GPInstall
  }
}

class PendingPurchaseFlowUseCaseMock(
  private val pendingPurchase: StartMode.PendingPurchaseFlow? = null
) : PendingPurchaseFlowUseCase {

  override operator fun invoke(): StartMode.PendingPurchaseFlow? {
    return pendingPurchase
  }
}
