package cm.aptoide.skills.entity

import cm.aptoide.skills.model.WalletAddress
import com.appcoins.wallet.core.network.eskills.model.QueueIdentifier

data class UserData(
    val userId: String,
    val roomId: String,
    val walletAddress: WalletAddress?,
    val session: String,
    val status: Status,
    val queueId: QueueIdentifier?
) {
  companion object {
    fun fromStatus(status: Status): UserData {
      return UserData("", "", null, "", status, null)
    }

    fun fromStatus(status: Status, queueId: QueueIdentifier?): UserData {
      return UserData("", "", null, "", status, queueId)
    }
  }

  enum class Status {
    IN_QUEUE,
    REFUNDED,
    COMPLETED,
    PAYING,
    FAILED
  }
}
