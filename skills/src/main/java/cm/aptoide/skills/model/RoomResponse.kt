package cm.aptoide.skills.model

import com.google.gson.annotations.SerializedName

class RoomResponse(
    @SerializedName("room_id")
    var roomId: String,

    @SerializedName("users")
    var users: List<User>,

    @SerializedName("package_name")
    var packageName: String,

    @SerializedName("room_stake")
    var roomStake: RoomStake
)