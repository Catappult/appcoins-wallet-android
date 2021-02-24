package com.appcoins.wallet.billing

import com.appcoins.wallet.bdsbilling.BdsApi
import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi

interface BillingDependenciesProvider {
  fun supportedVersion(): Int

  fun bdsApi(): BdsApi

  fun walletService(): WalletService

  fun proxyService(): ProxyService

  fun billingMessagesMapper(): BillingMessagesMapper

  fun bdsApiSecondary(): BdsApiSecondary

  fun subscriptionBillingService(): SubscriptionBillingApi
}