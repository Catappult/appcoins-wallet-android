package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

data class PayTicketRequest(

    @SerializedName("ticket_id")
    private val ticketId: String,

    @SerializedName("callback_url")
    private val callbackUrl: String
)
