package cm.aptoide.skills

import io.reactivex.Single

interface WalletAddressObtainer {

  fun getWalletAddress(): Single<String>
}