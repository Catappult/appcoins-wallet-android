package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import com.asfoundation.wallet.manage_cards.usecases.GetStoredCardsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetStoredCardsV2UseCase @Inject constructor(
  private val getStoredCardsUseCase: GetStoredCardsUseCase,
  @IoDispatcher private val networkDispatcher: CoroutineDispatcher,
  ) {

  suspend operator fun invoke() = getStoredCardsUseCase().callAsync(networkDispatcher)
}