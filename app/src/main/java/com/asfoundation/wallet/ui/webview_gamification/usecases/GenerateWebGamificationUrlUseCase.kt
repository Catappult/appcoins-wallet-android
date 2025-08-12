package com.asfoundation.wallet.ui.webview_gamification.usecases

import android.content.Context
import android.util.Log
import com.appcoins.wallet.core.utils.android_common.extensions.convertToBase64Url
import com.appcoins.wallet.core.utils.properties.HostProperties
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetCachedCurrencySymbolUseCase
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetCachedCurrencyUseCase
import com.asfoundation.wallet.ui.webview_payment.usecases.GetEncryptedPrivateKeyUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject

class GenerateWebGamificationUrlUseCase @Inject constructor(
  private val getEncryptedPrivateKeyUseCase: GetEncryptedPrivateKeyUseCase,
  private val getCachedCurrencyUseCase: GetCachedCurrencyUseCase,
  private val getCachedCurrencySymbolUseCase: GetCachedCurrencySymbolUseCase,
  @ApplicationContext private val context: Context,
) {

  operator fun invoke(
    userStatsBonusReceived: BigDecimal,
    userStatsAmount: BigDecimal,
    userStatsLevel: Int,
  ): Single<String> {
    return getEncryptedPrivateKeyUseCase()
      .map { encyptedKey ->
        val url =
          HostProperties.WEBVIEW_GAMIFICATION_URL +
              "?encodedData=${
                createEncodedData(
                  userStatsBonusReceived.toDouble(),
                  userStatsAmount.toDouble(),
                  userStatsLevel,
                  if (getCachedCurrencyUseCase().equals("null")) "" else getCachedCurrencyUseCase() ?: ""
                )
              }" +
              "&currencySymbol=${
                if (getCachedCurrencySymbolUseCase.equals("null")) "" else getCachedCurrencySymbolUseCase() ?: ""
              }"
        Log.d("Gamification", "url: $url")
        url
      }
  }

  fun createEncodedData(
    userStatsBonusReceived: Double,
    userStatsAmount: Double,
    userStatsLevel: Int,
    currency: String
  ): String {
    val userStats = mapOf(
      "userStatsBonusReceived" to userStatsBonusReceived,
      "userStatsAmount" to userStatsAmount,
      "userStatsLevel" to userStatsLevel + 1,
      "currency" to currency,
      "isWebViewVersion" to true,
      "comesFromWallet" to true,
    )

    val jsonObject = JSONObject().apply {
      userStats.forEach { (key, value) ->
        put(key, value)
      }
    }

    return jsonObject.toString().convertToBase64Url()

  }

}
