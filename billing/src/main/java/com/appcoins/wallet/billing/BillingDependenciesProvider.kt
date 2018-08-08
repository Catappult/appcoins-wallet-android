package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.RemoteRepository
import io.reactivex.Single

interface BillingDependenciesProvider {
  fun getSupportedVersion(): Int

  fun getBdsApi(): RemoteRepository.BdsApi

  fun getWalletService(): WalletService

  fun getProxyService(): ProxyService

}