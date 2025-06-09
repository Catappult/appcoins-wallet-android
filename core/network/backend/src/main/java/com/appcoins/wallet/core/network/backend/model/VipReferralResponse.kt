package com.appcoins.wallet.core.network.backend.model

import com.appcoins.wallet.core.network.backend.model.PromoCodeBonusResponse.App
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class VipReferralResponse(
  @SerializedName("code") val code: String,
  @SerializedName("earned_usd_amount") val earnedUsdAmount: String,
  @SerializedName("referrals") val referrals: String,
  @SerializedName("active") val active: Boolean,
  @SerializedName("revenue_share") val vipBonus: String,
  @SerializedName("end_date") val endDate: String,
  @SerializedName("start_date") val startDate: String,
  @SerializedName("app") val app: App,
  @SerializedName("earned_currency_amount") val earnedCurrencyAmount: String? = null,
) {

  private val simpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault(Locale.Category.FORMAT))
      .apply { timeZone = TimeZone.getTimeZone("UTC") }

  val startDateAsDate
    get() = runCatching { simpleDateFormat.parse(startDate) }.getOrNull()

  companion object {
    val invalidReferral =
      VipReferralResponse(
        code = "",
        earnedUsdAmount = "",
        referrals = "",
        active = false,
        vipBonus = "",
        endDate = "",
        startDate = "",
        app = App(packageName = "", appName = "", appIcon = "")
      )
  }
}
