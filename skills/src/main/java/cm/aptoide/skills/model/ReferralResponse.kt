package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

class ReferralResponse (
  @SerializedName("referral_code")
  var referral_code: String,

  @SerializedName("count")
  var count: Int,

  @SerializedName("active")
  var active: Boolean
  )
