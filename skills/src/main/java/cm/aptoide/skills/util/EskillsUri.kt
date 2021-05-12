package cm.aptoide.skills.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class EskillsUri(
  var scheme: String,
  var host: String,
  var path: String,
  var parameters: MutableMap<String, String?>
) {

  fun getUserId(): String {
    return parameters[EskillsParameters.USER_ID]!!
  }

  fun getUserName(): String {
    return parameters[EskillsParameters.USER_NAME]!!
  }

  fun getPackageName(): String {
    return parameters[EskillsParameters.DOMAIN]!!
  }

  fun getProduct(): String {
    return parameters[EskillsParameters.PRODUCT]!!
  }

  fun getPrice(): String {
    return parameters[EskillsParameters.VALUE]!!
  }

  fun getCurrency(): String {
    return parameters[EskillsParameters.CURRENCY]!!
  }

  fun getEnvironment(): MatchEnvironment? {
    return try {
      MatchEnvironment.valueOf(parameters[EskillsParameters.ENVIRONMENT]!!)
    } catch (e: IllegalArgumentException) {
      null
    }
  }

  fun getMetadata(): Map<String, String> {
    val metadata = parameters[EskillsParameters.METADATA]
    return if (metadata != null) {
      Gson().fromJson(metadata, object : TypeToken<Map<String?, String?>?>() {}.type)
    } else {
      emptyMap()
    }
  }

  enum class MatchEnvironment {
    LIVE, SANDBOX
  }
}