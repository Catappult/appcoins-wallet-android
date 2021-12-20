package cm.aptoide.skills.entity

data class UserData(
    val userId: String, val roomId: String, val walletAddress: String,
    val session: String, val status: Status, val queueId: String?
) {
  companion object {
    fun fromStatus(status: Status): UserData {
      return UserData(
          "",
          "",
          "",
          "",
          status,
          ""
      )
    }

    fun fromStatus(status: Status, queueId: String?): UserData {
      return UserData(
          "",
          "",
          "",
          "",
          status,
          queueId
      )
    }
  }

  enum class Status {
    IN_QUEUE, REFUNDED, COMPLETED, PAYING, FAILED
  }
}
