package com.appcoins.wallet.billing

import com.appcoins.wallet.billing.repository.RemoteRepository

interface BdsApiProvider {
  fun getBdsApi(): RemoteRepository.BdsApi
}