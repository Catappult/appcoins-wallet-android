package com.asfoundation.wallet.iab.presentation.verify

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.iab.FragmentNavigator
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class VerifyViewModel internal constructor(
  private val verifyNavigator: FragmentNavigator,
  private val displayChatUseCase: DisplayChatUseCase,
) : ViewModel() {

  fun launchChat() {
    displayChatUseCase()
  }

  fun popBackStack() {
    verifyNavigator.popBackStack()
  }

  fun navigateToVerify(context: Context) {
    verifyNavigator.startActivity(VerificationCreditCardActivity.newIntent(context))
  }
}

@HiltViewModel
class VerifyInjectionsProvider @Inject constructor(
  val displayChatUseCase: DisplayChatUseCase
) : ViewModel()

@Composable
fun rememberVerifyViewModel(
  navController: NavController,
): VerifyViewModel {
  val injectionsProvider = hiltViewModel<VerifyInjectionsProvider>()
  return viewModel<VerifyViewModel>(
    key = navController.hashCode().toString(),
    factory = object : Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return VerifyViewModel(
          verifyNavigator = FragmentNavigator(navController),
          displayChatUseCase = injectionsProvider.displayChatUseCase,
        ) as T
      }
    }
  )
}
