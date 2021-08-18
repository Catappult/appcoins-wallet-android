package cm.aptoide.skills.repository

import android.content.SharedPreferences
import io.reactivex.Single

class SharedPreferencesTicketLocalStorage(private val preferences: SharedPreferences) :
  TicketLocalStorage {
  companion object {
    private val PREFIX = "TICKET_LOCAL_STORAGE_PREFIX_"
  }

  override fun getTicketInQueue(walletAddress: String): Single<StoredTicket> {
    return Single.fromCallable {
      return@fromCallable preferences.getString(getKey(walletAddress), null)
        ?.let { StoredTicketInQueue(it) } ?: EmptyStoredTicket
    }
  }

  private fun getKey(walletAddress: String): String {
    return PREFIX + walletAddress
  }
}