package cm.aptoide.skills.util

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
    } catch(e: IllegalArgumentException) {
      null
    }
  }

  enum class MatchEnvironment {
    LIVE, SANDBOX
  }
}