package com.asfoundation.wallet.interact

import com.appcoins.wallet.core.utils.common.RxSchedulers
import com.asfoundation.wallet.entity.GasSettings
import com.asfoundation.wallet.repository.GasSettingsRepositoryType
import com.asfoundation.wallet.util.FakeSchedulers
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class FetchGasSettingsInteractTest {

  @Mock
  lateinit var gasSettingsRepositoryType: GasSettingsRepositoryType

  private val fakeSchedulers: RxSchedulers = FakeSchedulers()
  private lateinit var fetchGasSettingsInteract: FetchGasSettingsInteract

  @Before
  fun setUp() {
    fetchGasSettingsInteract = FetchGasSettingsInteract(gasSettingsRepositoryType, fakeSchedulers)
  }

  @Test
  fun fetch() {
    val observer = TestObserver<GasSettings>()

    val expectedGasSettings = GasSettings(BigDecimal.TEN, BigDecimal.ONE)

    `when`(gasSettingsRepositoryType.getGasSettings(true)).thenReturn(
        Single.just(expectedGasSettings))

    fetchGasSettingsInteract.fetch(true)
        .subscribe(observer)

    (fakeSchedulers.io as TestScheduler).triggerActions()
    (fakeSchedulers.main as TestScheduler).triggerActions()

    observer.assertNoErrors()
        .assertValue { it == expectedGasSettings }

  }
}