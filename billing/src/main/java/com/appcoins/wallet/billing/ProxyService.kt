package com.appcoins.wallet.billing

import io.reactivex.Single

interface ProxyService {

  fun getAppCoinsAddress(debug: Boolean): Single<String>

  fun getIabAddress(debug: Boolean): Single<String>
}