package cm.aptoide.skills.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RoomMetadata(@SerializedName("string")
                        @Expose
                        var string: String? = null, @SerializedName("string2")
                        @Expose
                        var string2: String? = null
)