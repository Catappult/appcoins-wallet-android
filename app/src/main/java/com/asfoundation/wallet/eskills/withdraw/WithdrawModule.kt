package com.asfoundation.wallet.eskills.withdraw

import androidx.fragment.app.Fragment
import com.asfoundation.wallet.eskills.withdraw.usecases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.GetStoredUserEmailUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.WithdrawToFiatUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
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
  fun providesWithdrawNavigator(fragment: Fragment): WithdrawNavigator {
    return WithdrawNavigator(fragment.requireActivity())
  }
}
