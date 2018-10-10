package com.appcoins.wallet.bdsbilling

import io.reactivex.Single

interface ProxyService {

  fun getAppCoinsAddress(debug: Boolean): Single<String>

  fun getIabAddress(debug: Boolean): Single<String>
}