package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.RemoteRepository

interface BillingDependenciesProvider {
  fun getSupportedVersion(): Int

  fun getBdsApi(): RemoteRepository.BdsApi

  fun getWalletService(): WalletService

  fun getBillingFactory(): BillingFactory

  fun getProxyService(): ProxyService
}