package com.asfoundation.wallet.ui.login.custom_tab_login.native_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.asfoundation.wallet.ui.login.custom_tab_login.CustomTabLoginActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The activity to be used in native login flow.
 *
 * This activity is used to be an anchor to close the [CustomTabsIntent] Activity
 * before processing the login request.
 *
 * This is necessary to avoid being stuck in the user's google account while processing
 * the login information. With this Activity the user see [MainActivity] instead,
 * improving the user experience.
 */
@AndroidEntryPoint
class NativeLoginActivity : ComponentActivity() {
  companion object {
    /**
     * The url to be used in [CustomTabsIntent]
     */
    const val URL = "cct_url"

    /**
     * The url to be used in [CustomTabLoginActivity]
     */
    const val DEEP_LINK = "deep_link"


    /**
     * The tag to identify [NativeLoginActivity] logger messages
     */
    private const val TAG = "NativeLoginActivity"

    /**
     * A flag to indicate to [CustomTabLoginActivity] that the login request
     * already passed through native login flow.
     */
    const val IS_FROM_NATIVE_LOGIN = "is_from_native_login"
  }

  /**
   * Logger instance
   */
  @Inject
  lateinit var logger: Logger

  /**
   * helper function to navigate to main activity [MainActivity]
   *
   * @param context - The context to be used in the intent
   * @param logCallback - The callback to be used in logging the event
   */
  private fun navigateToMain(
    context: Context,
    logCallback: () -> Unit
  ) {
    logCallback()
    Intent(context, MainActivity::class.java)
      .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP }
      .also { startActivity(it) }
    finish()
  }

  /**
   * helper function to add [IS_FROM_NATIVE_LOGIN] flag to the url
   * @receiver - The url to be modified
   *
   * @return - The modified url
   */
  private fun String.addIsFromNativeLogin(): String {
    val separator = if (contains("?")) "&" else "?"
    return "$this$separator$IS_FROM_NATIVE_LOGIN=true"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val cctUrl = intent
      .getStringExtra(URL)

    if (cctUrl == null) {
      navigateToMain(this) {
        logger.log(TAG, "CCT_URL not found in intent, navigating to main activity.")
      }
    } else {
      CustomTabsIntent
        .Builder()
        .build()
        .launchUrl(this, cctUrl.toUri())
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    val uri = intent?.getStringExtra(DEEP_LINK)
    if (uri == null) {
      navigateToMain(this) {
        logger.log(TAG, "DEEP_LINK not found in intent, navigating to main activity.")
      }
    } else {
      val intent = Intent(this, CustomTabLoginActivity::class.java)
      intent.data = uri
        .addIsFromNativeLogin()
        .toUri()
      startActivity(intent)
      finish()
    }
  }
}