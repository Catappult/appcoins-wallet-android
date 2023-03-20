package com.appcoins.wallet.billing

import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.core.network.bds.BdsApiSecondary
import com.appcoins.wallet.core.network.microservices.api.BrokerVerificationApi
import com.appcoins.wallet.core.network.microservices.api.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.SubscriptionBillingApi

interface BillingDependenciesProvider {
  fun supportedVersion(): Int

  fun brokerBdsApi(): BrokerVerificationApi.BrokerBdsApi

  fun inappApi(): InappBillingApi

  fun walletService(): WalletService

  fun proxyService(): ProxyService

  fun billingMessagesMapper(): BillingMessagesMapper

  fun bdsApiSecondary(): BdsApiSecondary

  fun subscriptionsApi(): SubscriptionBillingApi
}