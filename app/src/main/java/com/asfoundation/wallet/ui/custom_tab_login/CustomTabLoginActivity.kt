package com.asfoundation.wallet.ui.custom_tab_login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.ui.webview_login.WebViewLoginViewModel
import com.asfoundation.wallet.ui.webview_login.WebViewLoginViewModel.UiState.FinishActivity
import com.asfoundation.wallet.ui.webview_login.WebViewLoginViewModel.UiState.FinishWithError
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
    private const val TAG = "CustomTabLoginActivity"
  }

  @Inject
  lateinit var logger: Logger

  private val viewModel: WebViewLoginViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    logger.log(TAG, "CustomTab Activity created")
    val authToken = intent?.extras?.getString(AUTH_TOKEN)
    authToken?.let {
      viewModel.fetchUserKey(it)
    } ?: run {
      logger.log(TAG, "No auth token provided, finishing activity")
      Intent(this, MainActivity::class.java)
        .apply {
          flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }.also {
          startActivity(it)
        }
      finish()
    }

    lifecycleScope.launch {
      viewModel.uiState.collect { uiState ->
        when (uiState) {
          is FinishActivity -> {
            Intent(this@CustomTabLoginActivity, MainActivity::class.java)
              .apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
              }.also {
                startActivity(it)
              }
            finish()
          }

          is FinishWithError -> {
            Intent(this@CustomTabLoginActivity, MainActivity::class.java)
              .apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
              }.also {
                startActivity(it)
              }
            logger.log(TAG, "Error fetching user key, finishing activity")
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