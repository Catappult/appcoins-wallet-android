package com.asfoundation.wallet.eskills_withdraw

import com.asfoundation.wallet.eskills_withdraw.use_cases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills_withdraw.use_cases.GetStoredUserEmailUseCase
import com.asfoundation.wallet.eskills_withdraw.use_cases.WithdrawToFiatUseCase
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
