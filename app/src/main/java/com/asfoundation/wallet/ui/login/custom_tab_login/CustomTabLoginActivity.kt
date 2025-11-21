package com.asfoundation.wallet.ui.login.custom_tab_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark
import com.appcoins.wallet.ui.widgets.component.Animation
import com.asf.wallet.R
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.ui.login.LOGIN_CODE
import com.asfoundation.wallet.ui.login.RESPONSE_TOAST_MESSAGE
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity.NavigationCase.NAVIGATE_TO_MAIN_ACTIVITY
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity.NavigationCase.NAVIGATE_TO_WEBVIEW_PAYMENT_ACTIVITY
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.CustomTabLoginViewModel
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states.CustomTabVMStates.FinishActivity
import com.asfoundation.wallet.ui.login.custom_tab_login.viewModel.states.CustomTabVMStates.FinishWithError
import com.asfoundation.wallet.ui.webview_payment.PaymentOverlayHandle
import com.asfoundation.wallet.ui.webview_payment.WebViewPaymentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The LogMessage typealias is used to
 * represent a function that takes no parameters and returns Unit,
 * used to log messages in this activity.
 */
private typealias LogMessage = () -> Unit

/**
 * Activity to handle the login process via Custom Tabs.
 * This activity should not have any UI, it just handles the redirection
 * and fetches the user key using the provided auth token.
 * It then finishes and returns to the MainActivity.
 */
@AndroidEntryPoint
class CustomTabLoginActivity : ComponentActivity() {
  companion object {
    /**
     * The name of the auth token query parameter.
     */
    private const val AUTH_TOKEN = "auth_token"

    /**
     * The name of the email query parameter.
     */
    private const val EMAIL_TOKEN = "email"

    /**
     * The name of the is payment in process query parameter.
     */
    private const val IS_PAYMENT_IN_PROCESS = "is_payment_in_process"

    /**
     * The tag to identify [CustomTabLoginActivity] logger messages
     */
    private const val TAG = "CustomTabLoginActivity"

    /**
     * Default code to be used when [CustomTabLoginActivity] is not called to process a login.
     */
    const val LOGIN_NOT_PROCESSED = 201

    private const val LOADING_SIZE = 104
  }

  /**
   * possible navigation from [CustomTabLoginActivity]
   */
  private enum class NavigationCase {
    NAVIGATE_TO_WEBVIEW_PAYMENT_ACTIVITY,
    NAVIGATE_TO_MAIN_ACTIVITY,
  }

  /**
   * Logger to log messages from this activity
   */
  @Inject
  lateinit var logger: Logger

  /**
   * The view model to handle the login process via Custom Tabs.
   * @see [CustomTabLoginViewModel]
   */
  private val viewModel: CustomTabLoginViewModel by viewModels()

  /**
   * helper function to navigate to another activity
   * @param activity the activity to navigate from
   * @param to the destination to navigate to
   * @param logMessage the log message to log when navigating
   * @see [NavigationCase]
   */
  private fun navigate(
    activity: ComponentActivity = this,
    to: NavigationCase,
    logMessage: LogMessage? = null,
    buildResponseIntent: Intent.() -> Unit = {}
  ) {
    when (to) {
      NAVIGATE_TO_WEBVIEW_PAYMENT_ACTIVITY -> {
        navigateToWebViewActivity(
          intent = activity.intent,
          logMessage = logMessage
        )
      }

      NAVIGATE_TO_MAIN_ACTIVITY -> {
        navigateToMainActivity(
          context = activity,
          logMessage = logMessage,
          buildResponseIntent = buildResponseIntent
        )
      }
    }
  }

  /**
   * helper function to navigate to [MainActivity]
   * @param context the context to navigate from
   * @param logMessage the log message to log
   */
  private fun navigateToMainActivity(
    context: Context,
    logMessage: LogMessage?,
    buildResponseIntent: Intent.() -> Unit
  ) {
    logMessage?.invoke()
    Intent(context, MainActivity::class.java)
      .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
      .apply(buildResponseIntent)
      .also { startActivity(it) }
    finish()
  }

  /**
   * helper function to navigate to [WebViewPaymentActivity]
   * @param intent the intent to navigate from
   * @param logMessage the log message
   */
  private fun navigateToWebViewActivity(
    intent: Intent,
    logMessage: LogMessage?,
  ) {
    logMessage?.invoke()
    PaymentOverlayHandle.bringToFrontAndDeliver(uri = intent.data, clearTop = true)
    finish()
  }

  /**
   * helper function to get a boolean parameter from the intent
   * @param key the key of the parameter
   * @return the boolean parameter from the intent
   */
  private fun Intent?.getBoolParameter(key: String): Boolean =
    this?.data
      ?.getBooleanQueryParameter(key, false)
      ?: false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val authToken = intent.data?.getQueryParameter(AUTH_TOKEN)
    val email = intent.data?.getQueryParameter(EMAIL_TOKEN)
    val isPaymentInProcess = intent.getBoolParameter(IS_PAYMENT_IN_PROCESS)
    authToken
      ?.let { viewModel.fetchUserKey(it, email?.ifBlank { null }) }
      ?: run {
        navigate(
          activity = this,
          to = if (isPaymentInProcess) NAVIGATE_TO_WEBVIEW_PAYMENT_ACTIVITY else NAVIGATE_TO_MAIN_ACTIVITY,
          logMessage = if (isPaymentInProcess) null else {
            {
              logger.log(
                TAG,
                "Error fetching user key, no auth token provided. Navigating to Main Activity."
              )
            }
          }
        )
      }
    setContent {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = styleguide_dark,
      ) { paddingValues ->
        LoadingScreen(paddingValues)
      }
      lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
          viewModel.activityState.collect { uiState ->
            when (uiState) {
              is FinishActivity -> {
                navigate(
                  activity = this@CustomTabLoginActivity,
                  to = NAVIGATE_TO_MAIN_ACTIVITY,
                  buildResponseIntent = {
                    putExtra(LOGIN_CODE, uiState.response.code)
                    putExtra(RESPONSE_TOAST_MESSAGE, uiState.response.message)
                  }
                )
              }

              is FinishWithError -> {
                navigate(
                  activity = this@CustomTabLoginActivity,
                  to = NAVIGATE_TO_MAIN_ACTIVITY,
                  logMessage = {
                    logger.log(
                      TAG,
                      "Error fetching user key. Navigating to Main Activity."
                    )
                  }
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
  }

  /**
   * Composable to display the loading screen.
   * @param paddingValues the padding values to apply to the screen.
   */
  @Composable
  private fun LoadingScreen(paddingValues: PaddingValues) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Animation(modifier = Modifier.size(LOADING_SIZE.dp), animationRes = R.raw.loading_wallet)
    }
  }
}