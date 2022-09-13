package com.asfoundation.wallet.app_start

import com.asfoundation.wallet.gherkin.coScenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * AS a Wallet Developer,
 * I WANT to parse and map the referrer data,
 * FOR separation concerns
 */

@ExperimentalCoroutinesApi
internal class FirstUtmUseCaseTest {

  @Test
  fun `No UTM returns null`() = coScenario {
    val repository: FirstUtmRepository
    val useCase: FirstUtmUseCase

    m Given "Repository returns null UTM"
    repository = object : FirstUtmRepository {
      override suspend fun getReferrerUrl(): String? = null
    }
    useCase = FirstUtmUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)
  }

  @Test
  fun `OSP UTM after 5 seconds returns null`() = coScenario {
    val repository: FirstUtmRepository
    val useCase: FirstUtmUseCase

    m Given "Repository returns OSP UTM after 5 seconds"
    repository = object : FirstUtmRepository {
      override suspend fun getReferrerUrl(): String {
        delay(5000)
        return UTM_OSP
      }
    }
    useCase = FirstUtmUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)
  }

  @Test
  fun `OSP UTM returns FirstUtm`() = coScenario {
    val repository: FirstUtmRepository
    val useCase: FirstUtmUseCase

    m Given "Repository returns OSP UTM"
    repository = object : FirstUtmRepository {
      override suspend fun getReferrerUrl(): String = UTM_OSP
    }
    useCase = FirstUtmUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is data"
    assertEquals(
      StartMode.FirstUtm(
        sku = "13204",
        source = "aptoide",
        packageName = "com.igg.android.lordsmobile",
        integrationFlow = "osp"
      ),
      result
    )
  }

  @Test
  fun `SDK UTM returns FirstUtm`() = coScenario {
    val repository: FirstUtmRepository
    val useCase: FirstUtmUseCase

    m Given "Repository returns SDK UTM"
    repository = object : FirstUtmRepository {
      override suspend fun getReferrerUrl(): String = UTM_SDK
    }
    useCase = FirstUtmUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is data"
    assertEquals(
      StartMode.FirstUtm(
        sku = "13204",
        source = "aptoide",
        packageName = "com.igg.android.lordsmobile",
        integrationFlow = "sdk"
      ),
      result
    )

  }

  @Test
  fun `Wrong term UTM returns null`() = coScenario {
    val repository: FirstUtmRepository
    val useCase: FirstUtmUseCase

    m Given "Repository returns wrong term UTM"
    repository = object : FirstUtmRepository {
      override suspend fun getReferrerUrl(): String = UTM_WRONG_TERM
    }
    useCase = FirstUtmUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)

  }

  @Test
  fun `UTM without medium returns null`() = coScenario {
    val repository: FirstUtmRepository
    val useCase: FirstUtmUseCase

    m Given "Repository returns UTM without medium"
    repository = object : FirstUtmRepository {
      override suspend fun getReferrerUrl(): String = UTM_NO_MEDIUM
    }
    useCase = FirstUtmUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)

  }

  @Test
  fun `Invalid UTM returns null`() = coScenario {
    val repository: FirstUtmRepository
    val useCase: FirstUtmUseCase

    m Given "Repository returns invalid UTM"
    repository = object : FirstUtmRepository {
      override suspend fun getReferrerUrl(): String = UTM_INVALID
    }
    useCase = FirstUtmUseCaseImpl(repository)

    m When "Use case called for result"
    val result = useCase()

    m Then "result is null"
    assertNull(result)
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
  }
}
