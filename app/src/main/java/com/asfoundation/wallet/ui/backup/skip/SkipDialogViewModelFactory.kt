package com.asfoundation.wallet.ui.backup.skip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SkipDialogViewModelFactory :
    ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    return SkipDialogViewModel() as T
  }
}