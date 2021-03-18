package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

class RoomStake(
    @SerializedName("appc")
    var appc: Double,

    @SerializedName("usd")
    var usd: Int
)