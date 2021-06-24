package cm.aptoide.skills.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal

data class EskillsUri(
  var scheme: String,
  var host: String,
  var path: String,
  var parameters: MutableMap<String, String?>
) {

  fun getUserId(): String? {
    return parameters[EskillsParameters.USER_ID]
  }

  fun getUserName(): String? {
    return parameters[EskillsParameters.USER_NAME]
  }

  fun getPackageName(): String {
    return parameters[EskillsParameters.DOMAIN]!!
  }

  fun getProduct(): String? {
    return parameters[EskillsParameters.PRODUCT]
  }

  fun getPrice(): BigDecimal? {
    return parameters[EskillsParameters.VALUE]?.toBigDecimal()
  }

  fun getCurrency(): String? {
    return parameters[EskillsParameters.CURRENCY]
  }

  fun getEnvironment(): MatchEnvironment? {
    return try {
      val value = parameters[EskillsParameters.ENVIRONMENT]
      if (value != null) {
        return MatchEnvironment.valueOf(value)
      } else {
        return null
      }
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

  fun getNumberOfUsers(): Int? {
    return parameters[EskillsParameters.NUMBER_OF_USERS]?.toInt()
  }

  enum class MatchEnvironment {
    LIVE, SANDBOX
  }
}