package com.asfoundation.wallet.ui.login.custom_tab_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.CustomTabLoginViewModel
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states.CustomTabVMStates.FinishActivity
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states.CustomTabVMStates.FinishWithError
import com.asfoundation.wallet.ui.webview_payment.PaymentOverlayHandle
import com.asfoundation.wallet.ui.webview_payment.WebViewPaymentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity to handle the login process via Custom Tabs.
 * This activity should not have any UI, it just handles the redirection
 * and fetches the user key using the provided auth token.
 * It then finishes and returns to the MainActivity.
 */
@AndroidEntryPoint
class CustomTabLoginActivity : ComponentActivity() {
  companion object {
    private const val AUTH_TOKEN = "auth_token"
    private const val IS_PAYMENT_IN_PROCESS = "is_payment_in_process"
    private const val TAG = "CustomTabLoginActivity"
  }

  @Inject
  lateinit var logger: Logger

  private val viewModel: CustomTabLoginViewModel by viewModels()

  private fun navigate(
    context: Context = this,
    isPaymentInProcess: Boolean,
    logMessage: String? = null,
  ) {
    if (isPaymentInProcess) {
      navigateToWebViewActivity(
        context = context,
        logMessage = logMessage?.let { "$it. Navigating to WebViewActivity" }
          ?: "Navigating to WebViewActivity",
      )
    } else {
      navigateToMainActivity(
        context = context,
        logMessage = logMessage?.let { "$it. Navigating to MainActivity" }
          ?: "Navigating to MainActivity",
      )
    }
  }

  private fun navigateToMainActivity(
    context: Context = this,
    logMessage: String? = null,
  ) {
    logMessage?.let { Log.d(TAG, it) }
    Intent(context, MainActivity::class.java)
      .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
      .also { startActivity(it) }
    finish()
  }

  private fun navigateToWebViewActivity(
    context: Context = this,
    logMessage: String? = null,
  ) {
    logMessage?.let { Log.d(TAG, it) }
    PaymentOverlayHandle.bringToFrontAndDeliver(uri = intent.data)
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    logger.log(TAG, "CustomTab Activity created")
    val authToken = intent.data?.getQueryParameter(AUTH_TOKEN)
    val isPaymentInProcess =
      intent
        .data
        ?.getBooleanQueryParameter(IS_PAYMENT_IN_PROCESS, false)
        ?: false
    authToken
      ?.let { viewModel.fetchUserKey(it) }
      ?: run {
        navigate(
          context = this@CustomTabLoginActivity,
          isPaymentInProcess = isPaymentInProcess,
          logMessage = "No auth token provided"
        )
      }

    lifecycleScope.launch {
      viewModel.activityState.collect { uiState ->
        when (uiState) {
          is FinishActivity -> {
            navigate(
              context = this@CustomTabLoginActivity,
              isPaymentInProcess = isPaymentInProcess,
              logMessage = "User key fetched successfully",
            )
          }

          is FinishWithError -> {
            navigate(
              context = this@CustomTabLoginActivity,
              isPaymentInProcess = isPaymentInProcess,
              logMessage = "Error fetching user key",
            )
          }

          else -> {
            // no-op
          }
        }
      }
    }
  }
}