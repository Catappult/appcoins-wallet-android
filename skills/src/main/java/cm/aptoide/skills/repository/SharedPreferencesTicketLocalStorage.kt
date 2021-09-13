package cm.aptoide.skills.repository

import android.content.SharedPreferences
import cm.aptoide.skills.util.EskillsPaymentData
import com.google.gson.Gson
import io.reactivex.Single

class SharedPreferencesTicketLocalStorage(
  private val preferences: SharedPreferences,
  private val mapper: Gson
) :
  TicketLocalStorage {
  companion object {
    private val PREFIX = "TICKET_LOCAL_STORAGE_PREFIX_"
  }

  override fun getTicketInQueue(
    walletAddress: String,
    eskillsPaymentData: EskillsPaymentData
  ): Single<StoredTicket> {
    return Single.fromCallable {
      val data = getData(walletAddress)
      if (data != null && data == eskillsPaymentData) {
        return@fromCallable preferences.getString(getWalletKey(walletAddress), null)
          ?.let { StoredTicketInQueue(it) } ?: EmptyStoredTicket
      }
      return@fromCallable EmptyStoredTicket
    }
  }

  override fun saveTicketInQueue(
    walletAddress: String,
    ticketId: String,
    eskillsPaymentData: EskillsPaymentData
  ) {
    val editPreferences = preferences.edit()
    editPreferences.putString(getWalletKey(walletAddress), ticketId)
    editPreferences.putString(getDataKey(walletAddress), mapper.toJson(eskillsPaymentData))
    editPreferences.apply()
  }

  private fun getData(walletAddress: String): EskillsPaymentData? {
    return preferences.getString(getDataKey(walletAddress), null)
      ?.let { mapper.fromJson(it, EskillsPaymentData::class.java) }
  }

  private fun getWalletKey(walletAddress: String): String {
    return PREFIX + "WALLET_" + walletAddress
  }

  private fun getDataKey(walletAddress: String): String {
    return PREFIX + "DATA_" + walletAddress
  }
}