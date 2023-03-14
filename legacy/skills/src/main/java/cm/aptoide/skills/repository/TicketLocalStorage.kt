package cm.aptoide.skills.repository

import cm.aptoide.skills.model.WalletAddress
import cm.aptoide.skills.util.EskillsPaymentData
import io.reactivex.Single

interface TicketLocalStorage {

  fun getTicketInQueue(
    walletAddress: WalletAddress,
    eskillsPaymentData: EskillsPaymentData
  ): Single<StoredTicket>

  fun saveTicketInQueue(
    walletAddress: WalletAddress,
    ticketId: String,
    eskillsPaymentData: EskillsPaymentData
  )
}
