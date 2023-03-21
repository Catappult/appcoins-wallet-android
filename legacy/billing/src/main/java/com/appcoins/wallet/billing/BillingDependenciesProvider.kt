package com.appcoins.wallet.billing

import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.core.network.bds.api.BdsApiSecondary
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi

interface BillingDependenciesProvider {
  fun supportedVersion(): Int

  fun brokerBdsApi(): BrokerBdsApi

  fun inappApi(): InappBillingApi

  fun walletService(): WalletService

  fun proxyService(): ProxyService

  fun billingMessagesMapper(): BillingMessagesMapper

  fun bdsApiSecondary(): BdsApiSecondary

  fun subscriptionsApi(): SubscriptionBillingApi
}