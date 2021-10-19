package com.asfoundation.wallet.eskills_withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.eskills_withdraw.use_cases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills_withdraw.use_cases.GetStoredUserEmailUseCase
import com.asfoundation.wallet.eskills_withdraw.use_cases.WithdrawToFiatUseCase

class WithdrawViewModelFactory(
    private val getAvailableAmountToWithdrawUseCase: GetAvailableAmountToWithdrawUseCase,
    private val getStoredUserEmailUseCase: GetStoredUserEmailUseCase,
    private val withdrawToFiatUseCase: WithdrawToFiatUseCase
) : ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return WithdrawViewModel(getAvailableAmountToWithdrawUseCase, getStoredUserEmailUseCase,
        withdrawToFiatUseCase) as T
  }
}
