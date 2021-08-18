package cm.aptoide.skills.repository

import io.reactivex.Single

interface TicketLocalStorage {

  fun getTicketInQueue(walletAddress: String): Single<StoredTicket>
}
