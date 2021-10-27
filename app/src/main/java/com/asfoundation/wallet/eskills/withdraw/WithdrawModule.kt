package com.asfoundation.wallet.eskills.withdraw

import com.asfoundation.wallet.eskills.withdraw.usecases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.GetStoredUserEmailUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.WithdrawToFiatUseCase
import dagger.Module
import dagger.Provides

@Module
class WithdrawModule {

  @Provides
  fun providesWithdrawViewModelFactory(
      getAvailableAmountToWithdrawUseCase: GetAvailableAmountToWithdrawUseCase,
      getStoredUserEmailUseCase: GetStoredUserEmailUseCase,
      withdrawToFiatUseCase: WithdrawToFiatUseCase
  ): WithdrawViewModelFactory {
    return WithdrawViewModelFactory(getAvailableAmountToWithdrawUseCase, getStoredUserEmailUseCase,
        withdrawToFiatUseCase)
  }

  @Provides
  fun providesWithdrawNavigator(fragment: WithdrawFragment): WithdrawNavigator {
    return WithdrawNavigator(fragment.requireActivity())
  }
}
