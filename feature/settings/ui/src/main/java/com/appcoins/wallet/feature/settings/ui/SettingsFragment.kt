package com.appcoins.wallet.feature.settings.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.appcoins.wallet.core.utils.properties.PRIVACY_POLICY_URL
import com.appcoins.wallet.core.utils.properties.TERMS_CONDITIONS_URL
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TopBarTitle
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SettingsFragment : BasePageViewFragment() {

  private val viewModel: SettingsViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { SettingsView() } }
  }

  @Composable
  fun SettingsView() {
    Scaffold(
      topBar = {
        Surface { TopBar(onClickSupport = { viewModel.displayChat() }) }
      }, containerColor = WalletColors.styleguide_blue
    ) { padding ->
      SettingsScreen(paddingValues = padding)
    }
  }

  @Composable
  fun SettingsScreen(paddingValues: PaddingValues) {
    LazyColumn(modifier = Modifier.padding(paddingValues)) {
      item { TopBarTitle(stringResource(id = R.string.action_settings)) }
      item {
        SettingsCategories { SettingsItems() }
        SettingsCategories(title = stringResource(R.string.title_community)) {
          CommunitySettingsItems()
        }
        SettingsCategories(title = stringResource(R.string.title_support)) {
          SupportSettingsItems()
        }
        SettingsCategories(title = stringResource(R.string.title_opensource)) {
          DevelopmentSettingsItems()
        }
      }
    }

    when (viewModel.uiState.collectAsState().value) {
      SettingsViewModel.UiState.Error -> ShowError()
      SettingsViewModel.UiState.Loading -> CircularProgressIndicator()
      SettingsViewModel.UiState.Success -> {}
      else -> {}
    }
  }

  @Composable
  fun SettingsCategories(title: String? = null, settingsItems: @Composable () -> Unit = {}) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
      if (title != null) Text(
        text = title,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(8.dp),
        color = WalletColors.styleguide_dark_grey,
        style = MaterialTheme.typography.bodySmall
      )

      Card(
        colors = CardDefaults.cardColors(containerColor = WalletColors.styleguide_blue_secondary)
      ) {
        settingsItems()
      }
    }
  }

  @Composable
  fun SettingsItem(
    icon: Painter,
    title: String,
    subtitle: String? = null,
    action: () -> Unit,
    content: @Composable () -> Unit = {}
  ) {
    Button(
      onClick = action,
      colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
      shape = RoundedCornerShape(8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .padding(vertical = 16.dp)
          .fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            painter = icon,
            contentDescription = null,
            tint = WalletColors.styleguide_pink,
            modifier = Modifier.size(24.dp)
          )
          Column(modifier = Modifier.padding(start = 24.dp)) {
            Text(
              text = title,
              color = WalletColors.styleguide_light_grey,
              style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) Text(
              text = subtitle,
              color = WalletColors.styleguide_dark_grey,
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
        content()
      }
    }
  }

  @Composable
  fun CurrentCurrencySubItem(currency: String, icon: Painter) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(painter = icon, contentDescription = null, modifier = Modifier.size(24.dp))
      Text(
        text = currency,
        color = WalletColors.styleguide_medium_grey,
        modifier = Modifier.padding(horizontal = 8.dp)
      )
    }
  }

  @Composable
  fun FingerprintSwitch(switchON: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
      checked = switchON, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(
        checkedThumbColor = WalletColors.styleguide_pink,
        uncheckedThumbColor = WalletColors.styleguide_light_grey,
        checkedTrackColor = WalletColors.styleguide_grey_blue,
        uncheckedTrackColor = WalletColors.styleguide_grey_blue,
        checkedBorderColor = Color.Transparent,
        uncheckedBorderColor = Color.Transparent
      )
    )
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun CreditsMessage(openBottomSheet: Boolean, onDismissRequest: () -> Unit) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (openBottomSheet) {
      ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState,
        containerColor = WalletColors.styleguide_blue_secondary
      ) {
        Text(
          text = stringResource(R.string.settings_fragment_credits),
          color = WalletColors.styleguide_light_grey,
          modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp, top = 8.dp),
          textAlign = TextAlign.Center
        )
      }
    }
  }

  @Composable
  fun ShowError() {
    Snackbar { Text(text = stringResource(R.string.unknown_error)) }
  }

  @Composable
  fun SettingsItems() {
    var switchON by rememberSaveable { mutableStateOf(false) }

    SettingsItem(icon = painterResource(R.drawable.ic_manage_wallet),
      title = stringResource(R.string.manage_wallet_button),
      action = {})
    SettingsItem(icon = painterResource(R.drawable.ic_manage_subscriptions),
      title = stringResource(R.string.subscriptions_settings_title),
      action = {})
    SettingsItem(icon = painterResource(R.drawable.ic_currency),
      title = stringResource(R.string.change_currency_settings_title),
      action = {}) {
      CurrentCurrencySubItem("EUR", painterResource(R.drawable.ic_currency)) // TODO
    }
    SettingsItem(icon = painterResource(R.drawable.ic_settings_fingerprint),
      title = stringResource(R.string.fingerprint_settings),
      action = { switchON = !switchON }) {
      FingerprintSwitch(switchON = switchON) { changedSwitch -> switchON = changedSwitch }
    }
    SettingsItem(
      icon = painterResource(R.drawable.ic_updates),
      title = stringResource(R.string.check_updates_settings_title),
      subtitle = stringResource(id = R.string.check_updates_settings_subtitle, getAppVersion()),
      action = ::openAppStore
    )
  }

  @Composable
  fun CommunitySettingsItems() {
    SettingsItem(
      icon = painterResource(R.drawable.ic_twitter),
      title = stringResource(R.string.twitter),
      action = ::openTwitter
    )
    SettingsItem(
      icon = painterResource(R.drawable.ic_facebook),
      title = stringResource(R.string.facebook),
      action = ::openFacebook
    )
    SettingsItem(
      icon = painterResource(R.drawable.ic_telegram),
      title = stringResource(R.string.telegram),
      action = ::openTelegram
    )
  }

  @Composable
  fun SupportSettingsItems() {
    SettingsItem(
      icon = painterResource(R.drawable.ic_faqs),
      title = stringResource(R.string.faq),
      action = ::openFaqs
    )
    SettingsItem(
      icon = painterResource(R.drawable.ic_support),
      title = stringResource(R.string.settings_item_support),
      action = ::redirectToEmail
    )
    SettingsItem(
      icon = painterResource(R.drawable.ic_privacy_policy),
      title = stringResource(R.string.title_privacy_policy),
      action = ::openPrivacyPolicy
    )
    SettingsItem(
      icon = painterResource(R.drawable.ic_terms_conditions),
      title = stringResource(R.string.title_terms),
      action = ::openTermsConditions
    )
  }

  @Composable
  fun DevelopmentSettingsItems() {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    SettingsItem(
      icon = painterResource(R.drawable.ic_open_source),
      title = stringResource(R.string.title_source_code),
      action = ::openSourceCode
    )
    SettingsItem(icon = painterResource(R.drawable.ic_heart),
      title = stringResource(R.string.credits),
      action = { openBottomSheet = !openBottomSheet }) {
      CreditsMessage(openBottomSheet, onDismissRequest = { openBottomSheet = false })
    }
  }

  @Preview
  @Composable
  fun PreviewSettingsCategories() {
    SettingsCategories("Title") {
      SettingsItem(icon = painterResource(R.drawable.ic_currency),
        title = "Long text to test if layout brokes lorem ipsum dolor sit amet",
        action = {}) {
        CurrentCurrencySubItem(currency = "EUR", painterResource(R.drawable.ic_currency))
      }
      SettingsItem(icon = painterResource(R.drawable.ic_updates),
        title = stringResource(R.string.check_updates_settings_title),
        subtitle = "Current version: 2.8.7.0.0",
        action = {})
    }
  }

  private fun openUrl(url: String, newTaskFlag: Boolean = false) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      if (newTaskFlag) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      startActivity(intent)
    } catch (exception: ActivityNotFoundException) {
      exception.printStackTrace()
      // showError()
    }
  }

  private fun openSourceCode() = openUrl("https://github.com/Catappult/appcoins-wallet-android")

  private fun openFaqs() =
    openUrl("https://wallet.appcoins.io/faqs?lang=${Locale.getDefault().language}")

  private fun openTwitter() {
    try {
      activity?.packageManager?.getPackageInfo("com.twitter.android", 0)
      openUrl("twitter://user?user_id=915531221551255552", true)
    } catch (e: Exception) {
      openUrl("https://twitter.com/AppCoinsProject")
    }
  }

  private fun openTelegram() = openUrl("https://t.me/appcoinsofficial")

  private fun openFacebook() = openUrl("https://www.facebook.com/AppCoinsOfficial")

  private fun openPrivacyPolicy() =
    openUrl("$PRIVACY_POLICY_URL&lang=${Locale.getDefault().language}")

  private fun openTermsConditions() =
    openUrl("$TERMS_CONDITIONS_URL&lang=${Locale.getDefault().language}")

  private fun openAppStore() {
    try {
      startActivity(viewModel.buildIntentToStore())
    } catch (e: ActivityNotFoundException) {
      openUrl("https://wallet.appcoins.io/")
    }
  }

  private fun redirectToEmail() {
    val email = "info@appcoins.io"
    val subject = "Android wallet support question"
    val body = "Dear AppCoins support,"
    val emailAppIntent = Intent(Intent.ACTION_SENDTO)
    emailAppIntent.data = Uri.parse("mailto:")
    emailAppIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    emailAppIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    emailAppIntent.putExtra(Intent.EXTRA_TEXT, body)
    startActivity(emailAppIntent)
  }

  private fun getAppVersion(): String {
    var version = "N/A"
    try {
      activity?.let {
        val pInfo = it.packageManager.getPackageInfo(it.packageName, 0)
        version = pInfo.versionName
      }
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    return version
  }
}
