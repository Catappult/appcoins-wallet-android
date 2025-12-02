package com.asfoundation.wallet.currency_setup_cg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.asfoundation.wallet.currency_setup_cg.viewModel.SetUpCurrencyViewModel
import com.asfoundation.wallet.currency_setup_cg.viewModel.states.SetUpCurrencyVMState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetUpCurrencyActivity : ComponentActivity() {
  private val viewModel: SetUpCurrencyViewModel by viewModels()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.createWalletIfNeeded()
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.state.collect { state ->
          when (state) {
            is SetUpCurrencyVMState.Success -> {
              finish()
            }

            is SetUpCurrencyVMState.Error -> {
              finish()
            }

            else -> {
              // no-op
            }
          }
        }
      }
    }
  }
}