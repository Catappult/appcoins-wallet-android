package com.asfoundation.wallet.eskills.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.asfoundation.wallet.eskills.withdraw.usecases.GetAvailableAmountToWithdrawUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.GetStoredUserEmailUseCase
import com.asfoundation.wallet.eskills.withdraw.usecases.WithdrawToFiatUseCase

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
