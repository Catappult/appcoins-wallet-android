package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

class User(
    @SerializedName("wallet_address")
    var walletAddress: String,

    @SerializedName("user_id")
    var userId: String,

    @SerializedName("room_metadata")
    var roomMetadata: Map<String, String>,

    @SerializedName("score")
    var score: Any
)