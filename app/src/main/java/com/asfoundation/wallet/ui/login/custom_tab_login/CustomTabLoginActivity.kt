package com.asfoundation.wallet.ui.login.custom_tab_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.main.MainActivity
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity.NavigationCase.NAVIGATE_BACK_TO_NATIVE_LOGIN_ACTIVITY
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity.NavigationCase.NAVIGATE_TO_MAIN_ACTIVITY
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity.NavigationCase.NAVIGATE_TO_NATIVE_LOGIN_ACTIVITY
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity.NavigationCase.NAVIGATE_TO_WEBVIEW_PAYMENT_ACTIVITY
import com.asfoundation.wallet.ui.login.custom_tab_login.native_login.NativeLoginActivity
import com.asfoundation.wallet.ui.login.custom_tab_login.native_login.NativeLoginActivity.Companion.DEEP_LINK
import com.asfoundation.wallet.ui.login.custom_tab_login.native_login.NativeLoginActivity.Companion.IS_FROM_NATIVE_LOGIN
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
  }

  /**
   * possible navigation from [CustomTabLoginActivity]
   */
  private enum class NavigationCase {
    NAVIGATE_TO_NATIVE_LOGIN_ACTIVITY,
    NAVIGATE_TO_WEBVIEW_PAYMENT_ACTIVITY,
    NAVIGATE_TO_MAIN_ACTIVITY,
    NAVIGATE_BACK_TO_NATIVE_LOGIN_ACTIVITY,
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

      NAVIGATE_TO_NATIVE_LOGIN_ACTIVITY -> {
        navigateToNativeLoginActivity(
          context = activity,
          url = activity.intent.data?.toString()
        )
      }

      NAVIGATE_TO_MAIN_ACTIVITY -> {
        navigateToMainActivity(
          context = activity,
          logMessage = logMessage
        )
      }

      NAVIGATE_BACK_TO_NATIVE_LOGIN_ACTIVITY -> {
        navigateBackToNativeLoginActivity(
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
  ) {
    logMessage?.invoke()
    Intent(context, MainActivity::class.java)
      .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
      .also { startActivity(it) }
    finish()
  }

  /**
   * helper function to navigate to [NativeLoginActivity]
   * @param context the context to navigate from
   * @param url the url to navigate to
   */
  private fun navigateToNativeLoginActivity(
    context: Context,
    url: String?,
  ) {
    Intent(context, NativeLoginActivity::class.java)
      .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
      .apply { putExtra(DEEP_LINK, url) }
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
   * helper function to navigate back to [NativeLoginActivity].
   * This response contains the toast information to be display.
   *
   * @param buildResponseIntent the intent to be used as response in [NativeLoginActivity]
   *
   * @see [NativeLoginActivity.onNewIntent]
   */
  private fun navigateBackToNativeLoginActivity(
    buildResponseIntent: Intent.() -> Unit
  ) {
    Intent().apply(buildResponseIntent)
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val authToken = intent.data?.getQueryParameter(AUTH_TOKEN)
    val email = intent.data?.getQueryParameter(EMAIL_TOKEN)
    val isPaymentInProcess =
      intent
        .data
        ?.getBooleanQueryParameter(IS_PAYMENT_IN_PROCESS, false)
        ?: false
    val passedThroughNativeFlow =
      intent
        .data
        ?.getBooleanQueryParameter(IS_FROM_NATIVE_LOGIN, false)
        ?: false

    if (!passedThroughNativeFlow && !isPaymentInProcess) {
      navigate(
        activity = this@CustomTabLoginActivity,
        to = NAVIGATE_TO_NATIVE_LOGIN_ACTIVITY,
      )
    } else {
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
      lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
          viewModel.activityState.collect { uiState ->
            when (uiState) {
              is FinishActivity -> {
                navigate(
                  activity = this@CustomTabLoginActivity,
                  to = NAVIGATE_TO_MAIN_ACTIVITY,
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
}