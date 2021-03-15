package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

data class TicketResponse(
    @SerializedName("ticket_id")
    private val ticketId: String,

    @SerializedName("user_id")
    private val userId: String,

    @SerializedName("wallet_address")
    private val walletAddress: String,

    @SerializedName("room_metadata")
    private val roomMetadata: Map<String, String>
)