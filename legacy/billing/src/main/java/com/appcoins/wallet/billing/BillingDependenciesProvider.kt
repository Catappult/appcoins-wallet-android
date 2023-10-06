package com.appcoins.wallet.billing

import com.appcoins.wallet.bdsbilling.ProxyService
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.network.base.EwtAuthenticatorService
import com.appcoins.wallet.core.network.bds.api.BdsApiSecondary
import com.appcoins.wallet.core.network.microservices.api.broker.BrokerBdsApi
import com.appcoins.wallet.core.network.microservices.api.product.InappBillingApi
import com.appcoins.wallet.core.network.microservices.api.product.SubscriptionBillingApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService

interface BillingDependenciesProvider {
  fun supportedVersion(): Int

  fun brokerBdsApi(): BrokerBdsApi

  fun inappApi(): InappBillingApi

  fun walletService(): WalletService

  fun proxyService(): ProxyService

  fun billingMessagesMapper(): BillingMessagesMapper

  fun bdsApiSecondary(): BdsApiSecondary

  fun subscriptionsApi(): SubscriptionBillingApi

  fun rxSchedulers(): RxSchedulers

  fun ewtObtainer(): EwtAuthenticatorService

  fun partnerAddressService(): PartnerAddressService

}