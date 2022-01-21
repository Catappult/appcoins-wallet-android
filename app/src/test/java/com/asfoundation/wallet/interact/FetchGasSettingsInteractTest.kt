package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.repository.GasSettingsRepositoryType
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class FetchGasSettingsInteractTest {

  @Mock
  lateinit var gasSettingsRepositoryType: GasSettingsRepositoryType

  lateinit var networkTestScheduler: TestScheduler
  lateinit var viewTestScheduler: TestScheduler

  private lateinit var fetchGasSettingsInteract: FetchGasSettingsInteract

  @Before
  fun setUp() {
    networkTestScheduler = TestScheduler()
    viewTestScheduler = TestScheduler()
    fetchGasSettingsInteract =
        FetchGasSettingsInteract(gasSettingsRepositoryType, networkTestScheduler, viewTestScheduler)
  }

  @Test
  fun fetch() {
    val observer = TestObserver<GasSettings>()

    val expectedGasSettings = GasSettings(BigDecimal.TEN, BigDecimal.ONE)

    `when`(gasSettingsRepositoryType.getGasSettings(anyBoolean(), anyDouble())).thenReturn(
        Single.just(expectedGasSettings))

    fetchGasSettingsInteract.fetch(true)
        .subscribe(observer)

    networkTestScheduler.triggerActions()
    viewTestScheduler.triggerActions()

    observer.assertNoErrors()
        .assertValue { it == expectedGasSettings }

  }
}