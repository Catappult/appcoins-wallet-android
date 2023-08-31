package com.appcoins.wallet.core.network.eskills.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AppData(
  @JsonProperty("uname")
  var uname: String,

  @JsonProperty("name")
  var name: String,

  @JsonProperty("size")
  val size: Long,

  @JsonProperty("package")
  val packageName: String,

  @JsonProperty("icon")
  val appIcon: String,

  @JsonProperty("graphic")
  val background: String?,

  @JsonProperty("media")
  val media: Media,

  @JsonProperty("stats")
  val stats: Stats

)

data class Stats(
  @JsonProperty("rating")
  val rating: Rating,
  @JsonProperty("downloads")
  val downloads: Long
)

data class Rating(
  @JsonProperty("avg")
  val avg: Double
)

data class Media(
  @JsonProperty("description")
  val description: String,

  @JsonProperty("screenshots")
  val screenshots: List<Screenshot>?

)

data class Screenshot(
  @JsonProperty("url")
  val imageUrl: String,

  @JsonProperty("height")
  val height: Int,

  @JsonProperty("width")
  val width: Int
)