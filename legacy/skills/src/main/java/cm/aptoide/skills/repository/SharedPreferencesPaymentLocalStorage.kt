package cm.aptoide.skills.repository

import cm.aptoide.skills.model.CachedPayment
import cm.aptoide.skills.model.WalletAddress
import com.appcoins.wallet.sharedpreferences.EskillsPreferencesDataSource
import com.google.gson.Gson
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = PaymentLocalStorage::class)
class SharedPreferencesPaymentLocalStorage @Inject constructor(
  private val preferences: EskillsPreferencesDataSource,
  private val mapper: Gson
) : PaymentLocalStorage {
  companion object {
    private const val PREFIX = "PAYMENT_LOCAL_STORAGE_PREFIX_"
  }

  override fun save(cachedPayment: CachedPayment) =
    preferences.savePayments(
      getKey(cachedPayment.ticket.walletAddress),
      mapper.toJson(cachedPayment)
    )

  override fun get(walletAddress: WalletAddress): Single<CachedPayment> {
    return Single.fromCallable {
      return@fromCallable preferences.getStoredPayments(getKey(walletAddress))
        ?.let { mapper.fromJson(it, CachedPayment::class.java) } ?: throw RuntimeException(
        "Couldn't find any cached payment."
      )
    }
  }

  private fun getKey(walletAddress: WalletAddress): String {
    return PREFIX + walletAddress.address
  }
}
