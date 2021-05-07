package cm.aptoide.skills.util

data class EskillsUri(
    var scheme: String,
    var host: String,
    var path: String,
    var parameters: MutableMap<String, String>) {

  fun getUserId(): String {
    return parameters[EskillsParameters.USER_ID]!!
  }

  fun getPackageName(): String {
    return parameters[EskillsParameters.DOMAIN]!!
  }

  fun getProduct(): String {
    return parameters[EskillsParameters.PRODUCT]!!
  }

  fun getProductLabel(): String {
    return parameters[EskillsParameters.PRODUCT_LABEL]!!
  }

  fun getPrice(): String {
    return parameters[EskillsParameters.VALUE]!!
  }

  fun getCurrency(): String {
    return parameters[EskillsParameters.CURRENCY]!!
  }

  fun getFormattedPrice(): String {
    val value = parameters[EskillsParameters.VALUE]!!
    val currency = parameters[EskillsParameters.CURRENCY]!!
    return "$value $currency"
  }
}