package cm.aptoide.skills.model

import cm.aptoide.skills.util.EskillsUri
import com.google.gson.annotations.SerializedName

data class TicketRequest(

    @SerializedName("package_name")
    private val packageName: String,

    @SerializedName("user_id")
    private val userId: String,

    @SerializedName("user_name")
    private val userName: String,

    @SerializedName("wallet_address")
    private val walletAddress: String,

    @SerializedName("room_metadata")
    private val roomMetadata: Map<String, String>,

    @SerializedName("match_environment")
    private val matchEnvironment: EskillsUri.MatchEnvironment
)
