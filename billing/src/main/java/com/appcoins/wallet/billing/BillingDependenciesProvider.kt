package com.appcoins.wallet.billing

import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository

interface BillingDependenciesProvider {
  fun getSupportedVersion(): Int

  fun getBdsApi(): RemoteRepository.BdsApi

  fun getWalletService(): WalletService

  fun getProxyService(): ProxyService

  fun getBillingMessagesMapper(): BillingMessagesMapper

  fun getBdsApiSecondary(): BdsApiSecondary
}