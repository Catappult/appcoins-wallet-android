package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.GasServiceApi
import com.appcoins.wallet.core.network.backend.model.GasPrice
import com.asfoundation.wallet.entity.GasSettings
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.math.BigInteger

@RunWith(MockitoJUnitRunner::class)
class GasSettingsRepositoryTest {

  @Mock
  lateinit var gasServiceApi: GasServiceApi

  private lateinit var gasSettingsRepository: GasSettingsRepository
  private lateinit var gasPriceInteger: BigInteger
  private lateinit var gasPriceBigDecimal: BigDecimal
  private lateinit var gasPrice: GasPrice
  private lateinit var gasSettings: GasSettings

  @Before
  fun setUp() {
    gasSettingsRepository = GasSettingsRepository(gasServiceApi)
    gasPriceInteger = BigInteger(GAS_PRICE)
    gasPriceBigDecimal = BigDecimal(gasPriceInteger)
    gasPrice = GasPrice(gasPriceInteger)

    gasSettings = GasSettings(BigDecimal(BigInteger(GAS_PRICE)),
        BigDecimal(GasSettingsRepository.DEFAULT_GAS_LIMIT_FOR_TOKENS))
  }

  @Test
  fun whenFirstTime_shouldLoadFromNetwork() {
    `when`(gasServiceApi.getGasPrice()).thenReturn(Single.just(gasPrice))

    val observable = TestObserver<GasSettings>()

    gasSettingsRepository.getGasSettings(true)
        .subscribe(observable)

    val expected = GasSettings(gasPriceBigDecimal, BigDecimal(DEFAULT_GAS_LIMIT_FOR_TOKENS))

    observable
        .assertNoErrors()
        .assertValue { it.gasPrice == expected.gasPrice }
        .assertValue { it.gasLimit == expected.gasLimit }

    verify(gasServiceApi, times(1)).getGasPrice()
  }

  @Test
  fun whenException_shouldLoadDefault() {
    `when`(gasServiceApi.getGasPrice()).thenReturn(Single.error(Exception("A Random error")))

    val observable = TestObserver<GasSettings>()

    gasSettingsRepository.getGasSettings(true)
        .subscribe(observable)

    val expected =
        GasSettings(BigDecimal(DEFAULT_GAS_PRICE), BigDecimal(DEFAULT_GAS_LIMIT_FOR_TOKENS))

    observable
        .assertNoErrors()
        .assertValue { it.gasPrice == expected.gasPrice }
        .assertValue { it.gasLimit == expected.gasLimit }

    verify(gasServiceApi, times(1)).getGasPrice()
  }

  @Test
  fun whenNotTokenTransfer_shouldReturnDefault() {
    `when`(gasServiceApi.getGasPrice()).thenReturn(Single.just(gasPrice))

    val observable = TestObserver<GasSettings>()

    gasSettingsRepository.getGasSettings(false)
        .subscribe(observable)

    val expected = GasSettings(gasPriceBigDecimal, BigDecimal(
        GasSettingsRepository.DEFAULT_GAS_LIMIT))

    observable
        .assertNoErrors()
        .assertValue { it.gasPrice == expected.gasPrice }
        .assertValue { it.gasLimit == expected.gasLimit }

    verify(gasServiceApi, times(1)).getGasPrice()
  }

  companion object {
    const val GAS_PRICE = "50000000000"
    const val GAS_PRICE_CACHED = "70000000000"
    const val DEFAULT_GAS_PRICE = "30000000000"
    const val DEFAULT_GAS_LIMIT_FOR_TOKENS = "144000"
  }

}