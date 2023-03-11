package cm.aptoide.skills.interfaces

import cm.aptoide.skills.model.WalletAddress
import io.reactivex.Observable
import io.reactivex.Single

interface WalletAddressObtainer {
  fun getWalletAddress(): Single<WalletAddress>
  fun getOrCreateWallet(): Observable<String>
}
