package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository

interface BillingDependenciesProvider {
  fun getSupportedVersion(): Int

  fun getBdsApi(): RemoteRepository.BdsApi

  fun getWalletService(): WalletService

  fun getBillingFactory(): BillingFactory

  fun getProxyService(): ProxyService
}