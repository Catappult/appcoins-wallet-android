package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

data class TicketResponse(
    @SerializedName("ticket_id")
    val ticketId: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("wallet_address")
    val walletAddress: String,

    @SerializedName("room_metadata")
    val roomMetadata: Map<String, String>
)