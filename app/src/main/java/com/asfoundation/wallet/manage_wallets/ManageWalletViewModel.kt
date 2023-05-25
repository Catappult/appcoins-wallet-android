package com.asfoundation.wallet.manage_wallets

import androidx.lifecycle.ViewModel
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ManageWalletViewModel @Inject constructor(
  private val displayChatUseCase: DisplayChatUseCase
) : ViewModel() {

  fun displayChat() {
    displayChatUseCase()
  }
}