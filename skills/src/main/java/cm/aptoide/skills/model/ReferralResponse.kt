package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

data class ReferralResponse (
  @SerializedName("referral_code")
  val referral_code: String,

  @SerializedName("count")
  val count: Int,

  @SerializedName("active")
  val active: Boolean
  )