package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

data class TicketRequest(

    @SerializedName("user_id")
    private val userId: String,

    @SerializedName("wallet_address")
    private val walletAddress: String,

    @SerializedName("room_metadata")
    private val roomMetadata: Map<String, String>
)
