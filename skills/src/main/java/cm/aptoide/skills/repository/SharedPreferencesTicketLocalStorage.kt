package cm.aptoide.skills.repository

import android.content.SharedPreferences
import cm.aptoide.skills.model.WalletAddress
import cm.aptoide.skills.util.EskillsPaymentData
import com.google.gson.Gson
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = TicketLocalStorage::class)
class SharedPreferencesTicketLocalStorage @Inject constructor(
  private val preferences: SharedPreferences,
  private val mapper: Gson
) :
  TicketLocalStorage {
  companion object {
    private const val PREFIX = "TICKET_LOCAL_STORAGE_PREFIX_"
  }

  override fun getTicketInQueue(
    walletAddress: WalletAddress,
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
    walletAddress: WalletAddress,
    ticketId: String,
    eskillsPaymentData: EskillsPaymentData
  ) {
    val editPreferences = preferences.edit()
    editPreferences.putString(getWalletKey(walletAddress), ticketId)
    editPreferences.putString(getDataKey(walletAddress), mapper.toJson(eskillsPaymentData))
    editPreferences.apply()
  }

  private fun getData(walletAddress: WalletAddress): EskillsPaymentData? {
    return preferences.getString(getDataKey(walletAddress), null)
      ?.let { mapper.fromJson(it, EskillsPaymentData::class.java) }
  }

  private fun getWalletKey(walletAddress: WalletAddress): String {
    return PREFIX + "WALLET_" + walletAddress.address
  }

  private fun getDataKey(walletAddress: WalletAddress): String {
    return PREFIX + "DATA_" + walletAddress.address
  }
}