package cm.aptoide.skills.interfaces

import io.reactivex.Observable
import io.reactivex.Single

interface WalletAddressObtainer {
  fun getWalletAddress(): Single<String>
  fun getOrCreateWallet(): Observable<String>
}