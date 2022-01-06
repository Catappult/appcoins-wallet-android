package com.appcoins.wallet.billing

import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.bdsbilling.mappers.ExternalBillingSerializer
import com.appcoins.wallet.bdsbilling.repository.BdsApiSecondary
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi

interface BillingDependenciesProvider {
  fun supportedVersion(): Int

  fun brokerBdsApi(): RemoteRepository.BrokerBdsApi

  fun inappBdsApi(): RemoteRepository.InappBdsApi

  fun walletService(): WalletService

  fun proxyService(): ProxyService

  fun billingMessagesMapper(): BillingMessagesMapper

  fun bdsApiSecondary(): BdsApiSecondary

  fun subscriptionBillingService(): SubscriptionBillingApi

  fun billingSerializer(): ExternalBillingSerializer
}