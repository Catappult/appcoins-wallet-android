package cm.aptoide.skills.repository

import cm.aptoide.skills.model.*
import java.io.IOException

class TicketApiMapper {
  fun map(ticketResponse: TicketResponse): Ticket {
    return when (ticketResponse.ticketStatus) {
      TicketStatus.COMPLETED -> PurchasedTicket(ticketResponse.ticketId,
          ticketResponse.walletAddress, ticketResponse.userId, ticketResponse.roomId!!)
      else -> CreatedTicket(ticketResponse.ticketId,
          ProcessingStatus.fromTicketStatus(ticketResponse.ticketStatus),
          ticketResponse.walletAddress, ticketResponse.callbackUrl, ticketResponse.ticketPrice,
          ticketResponse.priceCurrency, ticketResponse.productToken)
    }
  }

  fun map(error: Throwable): Ticket {
    if (error.isNoNetworkException()) {
      return FailedTicket(ErrorStatus.NO_NETWORK)
    }
    // TODO
    return FailedTicket(ErrorStatus.REGION_NOT_SUPPORTED)
  }
}

fun Throwable?.isNoNetworkException(): Boolean {
  return this != null && (this is IOException || this.cause != null && this.cause is IOException)
}
