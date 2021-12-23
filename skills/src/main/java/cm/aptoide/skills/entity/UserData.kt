package cm.aptoide.skills.entity

import cm.aptoide.skills.model.WalletAddress

data class UserData(
    val userId: String, val roomId: String, val walletAddress: WalletAddress?,
    val session: String, val status: Status, val queueId: String?
) {
  companion object {
    fun fromStatus(status: Status): UserData {
      return UserData(
          "",
          "",
          null,
          "",
          status,
          ""
      )
    }

    fun fromStatus(status: Status, queueId: String?): UserData {
      return UserData(
          "",
          "",
          null,
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
