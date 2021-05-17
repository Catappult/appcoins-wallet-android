package cm.aptoide.skills.entity

data class UserData(
  val userId: String, val roomId: String, val walletAddress: String,
  val session: String, val refunded: Boolean = false
)