package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.RemoteRepository
import com.asf.appcoins.sdk.contractproxy.AppCoinsAddressProxySdk

interface BillingDependenciesProvider {
  fun getSupportedVersion(): Int

  fun getBdsApi(): RemoteRepository.BdsApi

  fun getWalletService(): WalletService

  fun getContractAddressProvider(): AppCoinsAddressProxySdk

  fun getBillingFactory(): BillingFactory
}