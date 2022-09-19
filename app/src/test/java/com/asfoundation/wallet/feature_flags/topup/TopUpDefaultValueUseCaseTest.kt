package com.asfoundation.wallet.feature_flags.topup

import com.asfoundation.wallet.feature_flags.FeatureFlag
import com.asfoundation.wallet.feature_flags.FeatureFlagsRepository
import com.asfoundation.wallet.feature_flags.topup.TopUpDefaultValueUseCase.Companion.FEATURE_FLAG_ID
import com.asfoundation.wallet.feature_flags.topup.TopUpDefaultValueUseCase.Companion.TOP_UP_EVENT
import com.asfoundation.wallet.gherkin.coScenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * AS a PO,
 * I WANT to experiment on top up default values,
 * FOR analysing it's impact on top up amounts
 */

@ExperimentalCoroutinesApi
internal class TopUpDefaultValueUseCaseTest {

  @DisplayName("On no feature flag returns null")
  @Test
  fun testNoVariant() = coScenario { scope ->
    m Given "feature flag repository that returns null"
    val repository = FeatureFlagsRepositoryMock(mutableListOf(null))
    m And "use case with the given repository"
    val useCase = TopUpDefaultValueUseCase(
      repository,
      AndroidIdRepositoryMock(),
      StandardTestDispatcher(scope.testScheduler)
    )

    m When "get variant"
    val variant = useCase.getVariant()

    m Then "variant is null"
    assertNull(variant)
    m And "no impressions sent"
    assertTrue(repository.impressions.isEmpty())
    m And "no events sent"
    assertTrue(repository.events.isEmpty())
  }

  @DisplayName("On feature flag returns variant")
  @Test
  fun testVariant() = coScenario { scope ->
    m Given "feature flag repository that returns a new one"
    val repository = FeatureFlagsRepositoryMock(
      mutableListOf(
        FeatureFlag(isNew = true, variant = "A", payload = "1")
      )
    )
    m And "use case with the given repository"
    val useCase = TopUpDefaultValueUseCase(
      repository,
      AndroidIdRepositoryMock(),
      StandardTestDispatcher(scope.testScheduler)
    )

    m When "get variant"
    val variant = useCase.getVariant()

    m Then "variant is 1"
    assertEquals(1, variant)
    m And "no impressions sent"
    assertTrue(repository.impressions.isEmpty())
    m And "no events sent"
    assertTrue(repository.events.isEmpty())
  }

  @DisplayName("On feature flag returns cached variant on repeats")
  @Test
  fun testCachedVariant() = coScenario { scope ->
    m Given "feature flag repository that returns a new one"
    val repository = FeatureFlagsRepositoryMock(
      mutableListOf(
        FeatureFlag(isNew = true, variant = "A", payload = "1")
      )
    )
    m And "use case with the given repository"
    val useCase = TopUpDefaultValueUseCase(
      repository,
      AndroidIdRepositoryMock(),
      StandardTestDispatcher(scope.testScheduler)
    )

    m When "get variant twice"
    val variant1 = useCase.getVariant()
    val variant2 = useCase.getVariant()

    m Then "both variants are 1"
    assertEquals(1, variant1)
    assertEquals(1, variant2)
    m And "no impressions sent"
    assertTrue(repository.impressions.isEmpty())
    m And "no events sent"
    assertTrue(repository.events.isEmpty())
  }

  @DisplayName("On feature flag with wrong payload returns null")
  @Test
  fun testWrongVariant() = coScenario { scope ->
    m Given "feature flag repository that returns a new one"
    val repository = FeatureFlagsRepositoryMock(
      mutableListOf(
        FeatureFlag(isNew = true, variant = "A", payload = "f")
      )
    )
    m And "use case with the given repository"
    val useCase = TopUpDefaultValueUseCase(
      repository,
      AndroidIdRepositoryMock(),
      StandardTestDispatcher(scope.testScheduler)
    )

    m When "get variant"
    val variant = useCase.getVariant()

    m Then "variant is null"
    assertNull(variant)
    m And "no impressions sent"
    assertTrue(repository.impressions.isEmpty())
    m And "no events sent"
    assertTrue(repository.events.isEmpty())
  }

  @DisplayName("On set impression sends only once")
  @Test
  fun testSendImpression() = coScenario { scope ->
    m Given "feature flag repository that returns a new one"
    val repository = FeatureFlagsRepositoryMock(mutableListOf())
    m And "use case with the given repository"
    val useCase = TopUpDefaultValueUseCase(
      repository,
      AndroidIdRepositoryMock(),
      StandardTestDispatcher(scope.testScheduler)
    )

    m When "get variant"
    useCase.setImpressed()

    m Then "one impressions sent"
    assertEquals(
      listOf(SentImpression(flagId = FEATURE_FLAG_ID, userId = "User")),
      repository.impressions
    )
    m And "no events sent"
    assertTrue(repository.events.isEmpty())
  }

  @DisplayName("On set top up event sends only once")
  @Test
  fun testSendEvent() = coScenario { scope ->
    m Given "feature flag repository that returns a new one"
    val repository = FeatureFlagsRepositoryMock(mutableListOf())
    m And "use case with the given repository"
    val useCase = TopUpDefaultValueUseCase(
      repository,
      AndroidIdRepositoryMock(),
      StandardTestDispatcher(scope.testScheduler)
    )

    m When "get variant"
    useCase.setTopUpWith(35.9)

    m Then "one event sent"
    assertEquals(
      listOf(
        SentEvent(
          flagId = FEATURE_FLAG_ID,
          userId = "User",
          name = TOP_UP_EVENT,
          payload = "35.9"
        )
      ),
      repository.events
    )
    m And "no impressions sent"
    assertTrue(repository.impressions.isEmpty())
  }

  internal data class SentImpression(val flagId: String, val userId: String)

  internal data class SentEvent(
    val flagId: String,
    val userId: String,
    val name: String,
    val payload: String
  )

  internal class AndroidIdRepositoryMock : AndroidIdRepository {
    override fun getAndroidId(): String = "User"
  }

  internal class FeatureFlagsRepositoryMock(private val data: MutableList<FeatureFlag?>) :
    FeatureFlagsRepository {
    internal val impressions: MutableList<SentImpression> = mutableListOf()
    internal val events: MutableList<SentEvent> = mutableListOf()

    override suspend fun getFeatureFlag(
      flagId: String,
      userId: String,
      profileData: Map<String, Any>
    ): FeatureFlag? {
      return data.removeFirst()
    }

    override suspend fun sendImpression(flagId: String, userId: String) {
      impressions.add(SentImpression(flagId, userId))
    }

    override suspend fun sendAction(
      flagId: String,
      userId: String,
      name: String,
      payload: String
    ) {
      events.add(SentEvent(flagId, userId, name, payload))
    }
  }
}