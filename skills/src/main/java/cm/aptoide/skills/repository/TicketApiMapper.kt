package cm.aptoide.skills.repository

import cm.aptoide.skills.model.*
import cm.aptoide.skills.util.getMessage
import cm.aptoide.skills.util.isNoNetworkException
import com.google.gson.Gson
import retrofit2.HttpException

class TicketApiMapper(private val jsonMapper: Gson) {
  companion object {
    private const val FORBIDDEN_CODE = 403
  }

  fun map(ticketResponse: TicketResponse): Ticket {
    return when (ticketResponse.ticketStatus) {
      TicketStatus.COMPLETED -> PurchasedTicket(ticketResponse.ticketId,
          ticketResponse.walletAddress, ticketResponse.userId, ticketResponse.roomId!!,
          ticketResponse.queueId)
      else -> CreatedTicket(ticketResponse.ticketId,
          ProcessingStatus.fromTicketStatus(ticketResponse.ticketStatus),
          ticketResponse.walletAddress, ticketResponse.callbackUrl, ticketResponse.ticketPrice,
          ticketResponse.priceCurrency, ticketResponse.productToken, ticketResponse.queueId)
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
    val response = jsonMapper.fromJson(exception.getMessage(), Response::class.java)
    return if (exception.code() == FORBIDDEN_CODE) {
      return when (response.detail.code) {
        ErrorCode.REGION_NOT_SUPPORTED -> FailedTicket(ErrorStatus.REGION_NOT_SUPPORTED)
        ErrorCode.NOT_AUTHENTICATED -> FailedTicket(ErrorStatus.GENERIC)
      }
    } else {
      FailedTicket(ErrorStatus.GENERIC)
    }
  }
}

data class Response(val detail: ErrorDetail)

data class ErrorDetail(val code: ErrorCode, val message: String)

enum class ErrorCode {
  REGION_NOT_SUPPORTED, NOT_AUTHENTICATED
}
