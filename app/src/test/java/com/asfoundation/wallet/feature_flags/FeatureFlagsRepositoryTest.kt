package com.asfoundation.wallet.feature_flags

import com.asfoundation.wallet.feature_flags.FeatureFlagsRepositoryImpl.Companion.IMPRESSION_EVENT
import com.asfoundation.wallet.feature_flags.api.*
import com.asfoundation.wallet.feature_flags.db.DBFeatureFlag
import com.asfoundation.wallet.feature_flags.db.FeatureFlagsDao
import com.asfoundation.wallet.gherkin.coScenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * AS a Wallet Developer,
 * I WANT to cache the requested feature flags in DB,
 * FOR keeping them in effect even if user has no network to receive or refresh the flag
 */

@ExperimentalCoroutinesApi
internal class FeatureFlagsRepositoryTest {

  @DisplayName("Get FF on API error")
  @ParameterizedTest(name = "{0}")
  @MethodSource("apiErrorDataProvider")
  fun `Get FF on API error`(
    comment: String,
    dbBefore: Set<DBFeatureFlag>,
    dbAfter: Set<DBFeatureFlag>,
    expectedResult: FeatureFlag?
  ) = coScenario {
    m Given "API mock throws error"
    val experimentsApiMock = ExperimentsApiMock()
    m And "DB with the given content"
    val featureFlagsDaoMock = FeatureFlagsDaoMock(dbBefore)
    m And "FeatureFlagsRepository with tne given API and DB"
    val repository = FeatureFlagsRepositoryImpl(experimentsApiMock, featureFlagsDaoMock)

    m When "getting a feature flag from FeatureFlagsRepository"
    val result = repository.getFeatureFlag("Experiment", "User", mapOf("version" to 123))

    m Then "DB has expected content"
    assertEquals(dbAfter, featureFlagsDaoMock.data)
    m And "result is as expected"
    assertEquals(expectedResult, result)
  }

  @DisplayName("Get FF on API legal success")
  @ParameterizedTest(name = "{0}")
  @MethodSource("apiLegalResultDataProvider")
  fun `Get FF on API legal success`(
    comment: String,
    apiAssignment: ApiAssignment,
    dbBefore: Set<DBFeatureFlag>,
    dbAfter: Set<DBFeatureFlag>,
    expectedResult: FeatureFlag
  ) = coScenario {
    m Given "API returns given assignment"
    val experimentsApiMock = ExperimentsApiMock(apiAssignment = apiAssignment)
    m And "DB with the given content"
    val featureFlagsDaoMock = FeatureFlagsDaoMock(dbBefore)
    m And "FeatureFlagsRepository with tne given API and DB"
    val repository = FeatureFlagsRepositoryImpl(experimentsApiMock, featureFlagsDaoMock)

    m When "getting a feature flag from FeatureFlagsRepository"
    val result = repository.getFeatureFlag("Experiment", "User", mapOf("version" to 123))

    m Then "DB has expected content"
    assertEquals(dbAfter, featureFlagsDaoMock.data)
    m And "result is as expected"
    assertEquals(expectedResult, result)
  }

  @DisplayName("Get FF on API illegal success & FF not in DB -> keep DB & return null")
  @ParameterizedTest(name = "{0}")
  @MethodSource("apiIllegalResultDataProvider")
  fun illegalSuccessNoDb(
    comment: String,
    apiAssignment: ApiAssignment
  ) = coScenario {
    m Given "API returns given assignment"
    val experimentsApiMock = ExperimentsApiMock(apiAssignment = apiAssignment)
    m And "DB has no such feature flag"
    val featureFlagsDaoMock = FeatureFlagsDaoMock(setOf(otherDBFeatureFlag))
    m And "FeatureFlagsRepository with tne given API and DB"
    val repository = FeatureFlagsRepositoryImpl(experimentsApiMock, featureFlagsDaoMock)

    m When "getting a feature flag from FeatureFlagsRepository"
    val result = repository.getFeatureFlag("Experiment", "User", mapOf("version" to 123))

    m Then "DB unchanged"
    assertEquals(setOf(otherDBFeatureFlag), featureFlagsDaoMock.data)
    m And "result is null"
    assertNull(result)
  }

  @DisplayName(value = "Get FF on API illegal success & FF in DB -> remove from DB & return null")
  @ParameterizedTest(name = "{0}")
  @MethodSource("apiIllegalResultDataProvider")
  fun illegalSuccessInDb(
    comment: String,
    apiAssignment: ApiAssignment
  ) = coScenario {
    m Given "API returns given assignment"
    val experimentsApiMock = ExperimentsApiMock(apiAssignment = apiAssignment)
    m And "DB has same feature flag"
    val featureFlagsDaoMock = FeatureFlagsDaoMock(setOf(otherDBFeatureFlag, oldDBFeatureFlag))
    m And "FeatureFlagsRepository with tne given API and DB"
    val repository = FeatureFlagsRepositoryImpl(experimentsApiMock, featureFlagsDaoMock)

    m When "getting a feature flag from FeatureFlagsRepository"
    val result = repository.getFeatureFlag("Experiment", "User", mapOf("version" to 123))

    m Then "DB without this feature flag"
    assertEquals(setOf(otherDBFeatureFlag), featureFlagsDaoMock.data)
    m And "result is null"
    assertNull(result)
  }

  @DisplayName("Send impression success")
  @Test
  fun `Send impression success`() = coScenario {
    m Given "API sends event successfully"
    val experimentsApiMock = ExperimentsApiMock(sentEvents = mutableListOf())
    m And "DB has something"
    val featureFlagsDaoMock = FeatureFlagsDaoMock(setOf(otherDBFeatureFlag, newDBFeatureFlag))
    m And "FeatureFlagsRepository with tne given API and DB"
    val repository = FeatureFlagsRepositoryImpl(experimentsApiMock, featureFlagsDaoMock)

    m When "trying to send an impression"
    repository.sendImpression("Experiment", "User")

    m Then "DB unchanged"
    assertEquals(setOf(otherDBFeatureFlag, newDBFeatureFlag), featureFlagsDaoMock.data)
    m And "impression event sent"
    assertEquals(
      listOf(
        SentEvent(
          experimentId = "Experiment",
          userId = "User",
          body = Events(listOf(Event(name = IMPRESSION_EVENT)))
        )
      ),
      experimentsApiMock.sentEvents
    )
  }

  @DisplayName("Send action success")
  @Test
  fun `Send action success`() = coScenario {
    m Given "API sends event successfully"
    val experimentsApiMock = ExperimentsApiMock(sentEvents = mutableListOf())
    m And "DB has something"
    val featureFlagsDaoMock = FeatureFlagsDaoMock(setOf(otherDBFeatureFlag, newDBFeatureFlag))
    m And "FeatureFlagsRepository with tne given API and DB"
    val repository = FeatureFlagsRepositoryImpl(experimentsApiMock, featureFlagsDaoMock)

    m When "trying to send an event"
    repository.sendAction(
      flagId = "Experiment",
      userId = "User",
      name = "Event",
      payload = "Payload"
    )

    m Then "DB unchanged"
    assertEquals(setOf(otherDBFeatureFlag, newDBFeatureFlag), featureFlagsDaoMock.data)
    m And "Custom event sent"
    assertEquals(
      listOf(
        SentEvent(
          experimentId = "Experiment",
          userId = "User",
          body = Events(listOf(Event(name = "Event", payload = "Payload")))
        )
      ),
      experimentsApiMock.sentEvents
    )
  }

  @DisplayName("Send impression failure ignored")
  @Test
  fun `Send impression failure ignored`() = coScenario {
    m Given "API sends event throws error"
    val experimentsApiMock = ExperimentsApiMock()
    m And "DB has something"
    val featureFlagsDaoMock = FeatureFlagsDaoMock(setOf(otherDBFeatureFlag, newDBFeatureFlag))
    m And "FeatureFlagsRepository with tne given API and DB"
    val repository = FeatureFlagsRepositoryImpl(experimentsApiMock, featureFlagsDaoMock)

    m When "trying to send an impression"
    repository.sendImpression("Experiment", "User")

    m Then "no crash happened"
    m And "DB unchanged"
    assertEquals(setOf(otherDBFeatureFlag, newDBFeatureFlag), featureFlagsDaoMock.data)
  }

  @DisplayName("Send action failure ignored")
  @Test
  fun `Send action failure ignored`() = coScenario {
    m Given "API sends event throws error"
    val experimentsApiMock = ExperimentsApiMock()
    m And "DB has something"
    val featureFlagsDaoMock = FeatureFlagsDaoMock(setOf(otherDBFeatureFlag, newDBFeatureFlag))
    m And "FeatureFlagsRepository with tne given API and DB"
    val repository = FeatureFlagsRepositoryImpl(experimentsApiMock, featureFlagsDaoMock)

    m When "trying to send an event"
    repository.sendAction(
      flagId = "Experiment",
      userId = "User",
      name = "Event",
      payload = "Payload"
    )

    m Then "no crash happened"
    m And "DB unchanged"
    assertEquals(setOf(otherDBFeatureFlag, newDBFeatureFlag), featureFlagsDaoMock.data)
  }

  @DisplayName("Send action with reserved name throws error")
  @Test
  fun `Send action with reserved name throws error`() = coScenario {
    m Given "API sends event successfully"
    val experimentsApiMock = ExperimentsApiMock(sentEvents = mutableListOf())
    m And "DB has something"
    val featureFlagsDaoMock = FeatureFlagsDaoMock(setOf(otherDBFeatureFlag, newDBFeatureFlag))
    m And "FeatureFlagsRepository with tne given API and DB"
    val repository = FeatureFlagsRepositoryImpl(experimentsApiMock, featureFlagsDaoMock)

    m When "trying to send an event"
    val thrown = assertThrows<IllegalArgumentException> {
      repository.sendAction(
        flagId = "Experiment",
        userId = "User",
        name = "IMPRESSION",
        payload = "Payload"
      )
    }

    m Then "DB unchanged"
    assertEquals(setOf(otherDBFeatureFlag, newDBFeatureFlag), featureFlagsDaoMock.data)
    m And "no events were sent"
    assertTrue(experimentsApiMock.sentEvents?.isEmpty() ?: false)
    m And "error was thrown with exact message"
    assertEquals(
      "IMPRESSION is reserved. Use recordImpression() for impressions",
      thrown.message
    )
  }

  companion object {
    @JvmStatic
    fun apiErrorDataProvider(): Stream<Arguments> = Stream.of(
      arguments(
        "Not in DB -> keep DB & return null",
        setOf(otherDBFeatureFlag),
        setOf(otherDBFeatureFlag),
        null
      ),
      arguments(
        "Old saved in DB -> keep DB & return saved old feature flag",
        setOf(oldDBFeatureFlag, otherDBFeatureFlag),
        setOf(oldDBFeatureFlag, otherDBFeatureFlag),
        oldFeatureFlag
      )
    )

    @JvmStatic
    fun apiLegalResultDataProvider(): Stream<Arguments> = Stream.of(
      arguments(
        "API New assignment & not in DB -> save to DB & return new feature flag",
        newApiAssignment,
        setOf(otherDBFeatureFlag),
        setOf(otherDBFeatureFlag, newDBFeatureFlag),
        newFeatureFlag
      ),
      arguments(
        "API Existing assignment & not in DB -> save to DB & return old feature flag",
        existingApiAssignment,
        setOf(otherDBFeatureFlag),
        setOf(otherDBFeatureFlag, oldDBFeatureFlag),
        oldFeatureFlag
      ),
      arguments(
        "API New assignment & old saved in DB -> replace in DB & return new feature flag",
        newApiAssignment,
        setOf(oldDBFeatureFlag, otherDBFeatureFlag),
        setOf(otherDBFeatureFlag, newDBFeatureFlag),
        newFeatureFlag
      ),
      arguments(
        "API Existing assignment & new saved in DB -> replace in DB & return new feature flag",
        existingApiAssignment,
        setOf(newDBFeatureFlag, otherDBFeatureFlag),
        setOf(otherDBFeatureFlag, oldDBFeatureFlag),
        oldFeatureFlag
      )
    )

    @JvmStatic
    fun apiIllegalResultDataProvider(): Stream<Arguments> = Stream.of(
      arguments(
        "API experiment not found",
        illegalApiAssignment(Status.EXPERIMENT_NOT_FOUND)
      ),
      arguments(
        "API experiment not started",
        illegalApiAssignment(Status.EXPERIMENT_NOT_STARTED)
      ),
      arguments(
        "API experiment in draft",
        illegalApiAssignment(Status.EXPERIMENT_IN_DRAFT_STATE)
      ),
      arguments(
        "API experiment paused",
        illegalApiAssignment(Status.EXPERIMENT_PAUSED)
      ),
      arguments(
        "API experiment no profile",
        illegalApiAssignment(Status.NO_PROFILE_MATCH)
      ),
      arguments(
        "API experiment expired",
        illegalApiAssignment(Status.EXPERIMENT_EXPIRED)
      ),
      arguments(
        "API experiment assignment failed",
        illegalApiAssignment(Status.ASSIGNMENT_FAILED)
      ),
      arguments(
        "API experiment no open buckets",
        illegalApiAssignment(Status.NO_OPEN_BUCKETS)
      ),
    )

    private val oldFeatureFlag = FeatureFlag(
      isNew = false,
      variant = "variantA",
      payload = "OldPayload"
    )
    private val newFeatureFlag = FeatureFlag(
      isNew = true,
      variant = "variantB",
      payload = "NewPayload"
    )

    private val existingApiAssignment = ApiAssignment(
      cache = false,
      payload = "OldPayload",
      assignment = "variantA",
      context = "PROD",
      status = Status.EXISTING_ASSIGNMENT
    )
    private val newApiAssignment = ApiAssignment(
      cache = true,
      payload = "NewPayload",
      assignment = "variantB",
      context = "PROD",
      status = Status.NEW_ASSIGNMENT
    )

    private fun illegalApiAssignment(status: Status) = ApiAssignment(
      cache = false, payload = "", assignment = "", context = "", status = status
    )

    private val oldDBFeatureFlag = DBFeatureFlag(
      flagId = "Experiment",
      variant = "variantA",
      payload = "OldPayload",
    )
    private val newDBFeatureFlag = DBFeatureFlag(
      flagId = "Experiment",
      variant = "variantB",
      payload = "NewPayload",
    )
    private val otherDBFeatureFlag = DBFeatureFlag(
      flagId = "Experiment2",
      variant = "variantC",
      payload = "OtherPayload",
    )
  }

  internal data class SentEvent(val experimentId: String, val userId: String, val body: Events)

  internal class ExperimentsApiMock(
    private val apiAssignment: ApiAssignment? = null,
    internal val sentEvents: MutableList<SentEvent>? = null
  ) : ExperimentsApi {

    override suspend fun assignExperiment(
      experimentId: String,
      userId: String,
      body: UserProfile
    ): ApiAssignment = apiAssignment ?: throw RuntimeException()

    override suspend fun sendEvent(experimentId: String, userId: String, body: Events) {
      sentEvents?.add(SentEvent(experimentId, userId, body)) ?: throw RuntimeException()
    }
  }

  internal class FeatureFlagsDaoMock(data: Set<DBFeatureFlag>) : FeatureFlagsDao {
    internal val data: MutableSet<DBFeatureFlag> = data.toMutableSet()

    override suspend fun get(flagId: String): DBFeatureFlag? =
      data.find { it.flagId == flagId }

    override suspend fun save(flag: DBFeatureFlag) {
      data.removeIf { it.flagId == flag.flagId }
      data.add(flag)
    }

    override suspend fun remove(flag: DBFeatureFlag) {
      data.removeIf { it.flagId == flag.flagId }
    }
  }
}
