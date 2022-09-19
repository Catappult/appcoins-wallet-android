package com.asfoundation.wallet.feature_flags.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface ExperimentsApi {
  // curl -H "Content-Type: application/json"
  //      -d "{\"profile\":{\"key1\":\"value1\",\"key2\":\"value2\"}}"
  //      https://abtest.aptoide.com/api/v1/assignments/applications/WalletApp/experiments/.../users/...
  @Headers("Content-Type: application/json")
  @POST("assignments/applications/WalletApp/experiments/{experimentId}/users/{userId}")
  suspend fun assignExperiment(
    @Path(value = "experimentId") experimentId: String,
    @Path(value = "userId") userId: String,
    @Body body: UserProfile
  ): ApiAssignment

  // curl -H "Content-Type: application/json"
  //      -d "{\"events\":[{\"name\":\"IMPRESSION\"}]}"
  //      https://abtest.aptoide.com/api/v1/events/applications/WalletApp/experiments/.../users/...
  // curl -H "Content-Type: application/json"
  //      -d "{\"events\":[{\"name\":\"myEventName\",\"payload\":\"{\\\"myPayloadKey1\\\":\\\"payloadKey1Value\\\"}\"}]}"
  //      https://abtest.aptoide.com/api/v1/events/applications/WalletApp/experiments/.../users/...
  @Headers("Content-Type: application/json")
  @POST("events/applications/WalletApp/experiments/{experimentId}/users/{userId}")
  suspend fun sendEvent(
    @Path(value = "experimentId") experimentId: String,
    @Path(value = "userId") userId: String,
    @Body body: Events
  )
}

data class UserProfile(val profile: Map<String, Any>)

data class Event(val name: String, val payload: String? = null)

data class Events(val events: List<Event>)

data class ApiAssignment(
  val cache: Boolean,
  val payload: String,
  val assignment: String,
  val context: String,
  val status: Status,
)

@Suppress("unused")
enum class Status {
  EXPERIMENT_NOT_FOUND,
  EXPERIMENT_NOT_STARTED,
  EXPERIMENT_IN_DRAFT_STATE,
  EXPERIMENT_PAUSED,
  NO_PROFILE_MATCH,
  EXPERIMENT_EXPIRED,
  ASSIGNMENT_FAILED,
  EXISTING_ASSIGNMENT,
  NEW_ASSIGNMENT,
  NO_OPEN_BUCKETS
}

