package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

class User(
    @SerializedName("wallet_address")
    var walletAddress: String,

    @SerializedName("user_id")
    var userId: String,

    @SerializedName("ticket_id")
    var ticketId: String,

    @SerializedName("room_metadata")
    var roomMetadata: Map<String, String>,

    @SerializedName("status")
    var status: UserStatus
)