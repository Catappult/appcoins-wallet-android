package cm.aptoide.skills.repository

import cm.aptoide.skills.model.WalletAddress
import com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData
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
