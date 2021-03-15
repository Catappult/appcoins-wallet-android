package cm.aptoide.skills.repository

import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import io.reactivex.Observable

class TicketsRepository(val ticketApi: TicketApi) {

  fun createTicket(): Observable<TicketResponse> {
    return ticketApi.postTicket(
        buildTicketRequest())
        .toObservable()
  }

  private fun buildTicketRequest() =
      TicketRequest("string_user_id", "0x3984723948723849", "0x3984723948723849", emptyMap())
}