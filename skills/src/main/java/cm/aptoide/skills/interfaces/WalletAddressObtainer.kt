package cm.aptoide.skills.interfaces

import io.reactivex.Single

interface WalletAddressObtainer {

  fun getWalletAddress(): Single<String>
}