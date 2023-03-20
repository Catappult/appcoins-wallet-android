package cm.aptoide.skills.repository

import cm.aptoide.skills.model.WalletAddress
import com.appcoins.wallet.core.network.eskills.model.EskillsPaymentData
import com.appcoins.wallet.sharedpreferences.EskillsPreferencesDataSource
import com.google.gson.Gson
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = TicketLocalStorage::class)
class SharedPreferencesTicketLocalStorage @Inject constructor(
  private val preferences: EskillsPreferencesDataSource,
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
        return@fromCallable preferences.getTicketInQueue(getWalletKey(walletAddress))
          ?.let { StoredTicketInQueue(it) } ?: EmptyStoredTicket
      }
      return@fromCallable EmptyStoredTicket
    }
  }

  override fun saveTicketInQueue(
    walletAddress: WalletAddress,
    ticketId: String,
    eskillsPaymentData: EskillsPaymentData
  ) =
    preferences.saveTicketInQueue(
      getWalletKey(walletAddress),
      ticketId,
      getDataKey(walletAddress),
      mapper.toJson(eskillsPaymentData)
    )

  private fun getData(walletAddress: WalletAddress): EskillsPaymentData? {
    return preferences.getTicketData(getDataKey(walletAddress))
      ?.let { mapper.fromJson(it, EskillsPaymentData::class.java) }
  }

  private fun getWalletKey(walletAddress: WalletAddress): String {
    return PREFIX + "WALLET_" + walletAddress.address
  }

  private fun getDataKey(walletAddress: WalletAddress): String {
    return PREFIX + "DATA_" + walletAddress.address
  }
}