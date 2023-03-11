package cm.aptoide.skills.repository

import cm.aptoide.skills.model.CachedPayment
import cm.aptoide.skills.model.WalletAddress
import io.reactivex.Single

interface PaymentLocalStorage {
  fun save(cachedPayment: CachedPayment)
  fun get(walletAddress: WalletAddress): Single<CachedPayment>
}
