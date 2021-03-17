package cm.aptoide.skills.repository

import cm.aptoide.skills.WalletAddressObtainer
import cm.aptoide.skills.api.TicketApi
import cm.aptoide.skills.model.TicketRequest
import cm.aptoide.skills.model.TicketResponse
import io.reactivex.Observable

class TicketsRepository(private val walletAddressObtainer: WalletAddressObtainer,
                        private val ticketApi: TicketApi) {

  fun createTicket(): Observable<TicketResponse> {
    return walletAddressObtainer.getWalletAddress()
        .flatMap {
          ticketApi.postTicket(
              buildTicketRequest(it))
        }
        .toObservable()
  }

  private fun buildTicketRequest(walletAddress: String) =
      TicketRequest("string_user_id", walletAddress, "0x3984723948723849", emptyMap())
}