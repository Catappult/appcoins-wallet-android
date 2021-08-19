package cm.aptoide.skills.repository

import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Single

interface TicketLocalStorage {

  fun getTicketInQueue(
    walletAddress: String,
    eskillsPaymentData: EskillsPaymentData
  ): Single<StoredTicket>

  fun saveTicketInQueue(
    walletAddress: String,
    ticketId: String,
    eskillsPaymentData: EskillsPaymentData
  )
}
