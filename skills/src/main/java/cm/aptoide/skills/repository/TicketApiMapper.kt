package cm.aptoide.skills.repository

import cm.aptoide.skills.model.*
import retrofit2.HttpException
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
    return when {
      error.isNoNetworkException() -> FailedTicket(ErrorStatus.NO_NETWORK)
      error is HttpException -> mapHttpException(error)
      else -> FailedTicket(ErrorStatus.GENERIC)
    }
  }

  private fun mapHttpException(exception: HttpException): FailedTicket {
    return when (exception.code()) {
      403 -> FailedTicket(ErrorStatus.REGION_NOT_SUPPORTED)
      else -> FailedTicket(ErrorStatus.GENERIC)
    }
  }
}

fun Throwable?.isNoNetworkException(): Boolean {
  return this != null && (this is IOException || this.cause != null && this.cause is IOException)
}
