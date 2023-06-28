package cm.aptoide.skills.util

import android.net.Uri
import com.appcoins.wallet.core.network.eskills.model.EskillsEndgameData
import com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

  fun Uri.parseStartGame(): EskillsPaymentData {
    val scheme = scheme
    val host = host
    val path = path
    val parameters = mutableMapOf<String, String?>()
    parameters.apply {
      for (key in queryParameterNames) {
        this[key] = getQueryParameter(key)
      }
    }
    val userId = parameters[EskillsParameters.USER_ID]
    val userName = parameters[EskillsParameters.USER_NAME]
    val domain = parameters[EskillsParameters.DOMAIN]!!
    val product = parameters[EskillsParameters.PRODUCT]
    val price = parameters[EskillsParameters.VALUE]?.toBigDecimal()
    val currency = parameters[EskillsParameters.CURRENCY]
    val timeout = parameters[EskillsParameters.TIMEOUT]?.toInt()
    val environment = getEnvironment(parameters)
    val metadata = getMetadata(parameters)
    val numberOfUsers = parameters[EskillsParameters.NUMBER_OF_USERS]?.toInt()
    return EskillsPaymentData(
      scheme!!, host!!, path!!, parameters, userId, userName, domain,
      product,
      price, currency, environment, metadata, numberOfUsers, timeout
    )
  }

fun Uri.parseEndgame(): EskillsEndgameData {
  val scheme = scheme
  val host = host
  val path = path
  val parameters = mutableMapOf<String, String?>()
  parameters.apply {
    for (key in queryParameterNames) {
      this[key] = getQueryParameter(key)
    }
  }
  val session = parameters[EskillsParameters.SESSION]!!
  val domain = parameters[EskillsParameters.DOMAIN]!!
  return EskillsEndgameData(
    scheme!!, host!!, path!!, parameters, domain, session)
}

  private fun getMetadata(parameters: MutableMap<String, String?>): Map<String, String> {
    val metadata = parameters[EskillsParameters.METADATA]
    return if (metadata != null) {
      Gson().fromJson(metadata, object : TypeToken<Map<String?, String?>?>() {}.type)
    } else {
      emptyMap()
    }
  }

  private fun getEnvironment(
    parameters: MutableMap<String, String?>
  ): EskillsPaymentData.MatchEnvironment? {
    return try {
      val value = parameters[EskillsParameters.ENVIRONMENT]
      if (value != null) {
        return EskillsPaymentData.MatchEnvironment.valueOf(
          value
        )
      } else {
        return null
      }
    } catch (e: IllegalArgumentException) {
      null
    }
  }