package cm.aptoide.skills.model

import java.math.BigDecimal

sealed class Ticket

data class CreatedTicket(
  val ticketId: String,
  val processingStatus: ProcessingStatus,
  val walletAddress: WalletAddress,
  val callbackUrl: String,
  val ticketPrice: BigDecimal,
  val priceCurrency: String,
  val productToken: String,
  val queueId: QueueIdentifier
) : Ticket()

enum class ProcessingStatus {
  PENDING_PAYMENT, REFUNDING, REFUNDED, IN_QUEUE;

  companion object {
    fun fromTicketStatus(status: TicketStatus): ProcessingStatus {
      return when (status) {
        TicketStatus.PENDING_PAYMENT -> PENDING_PAYMENT
        TicketStatus.REFUNDING -> REFUNDING
        TicketStatus.REFUNDED -> REFUNDED
        TicketStatus.IN_QUEUE -> IN_QUEUE
        else -> throw RuntimeException("Status not supported.")
      }
    }
  }
}

data class FailedTicket(
  val status: ErrorStatus
) : Ticket()

enum class ErrorStatus {
  REGION_NOT_SUPPORTED, NO_NETWORK, GENERIC, WALLET_VERSION_NOT_SUPPORTED
}

data class PurchasedTicket(
  val ticketId: String,
  val walletAddress: WalletAddress,
  val userId: String,
  val roomId: String,
  val queueId: QueueIdentifier
) : Ticket()
