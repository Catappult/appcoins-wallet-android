package com.appcoins.wallet.feature.settings.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TopBarTitle
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BasePageViewFragment() {

  private val viewModel: SettingsViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { SettingsView() } }
  }

  @Composable
  fun SettingsView() {
    Scaffold(
      topBar = {
        Surface { TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() }) }
      },
      containerColor = WalletColors.styleguide_blue
    ) { padding ->
      SettingsScreen(paddingValues = padding)
    }
  }
}

@Composable
fun SettingsScreen(paddingValues: PaddingValues) {
  LazyColumn(modifier = Modifier.padding(paddingValues)) {
    item {
      TopBarTitle(stringResource(id = R.string.action_settings))
    }
    item {
      SettingsCategories { SettingsItems() }
      SettingsCategories(title = stringResource(R.string.title_community)) { CommunitySettingsItems() }
      SettingsCategories(title = stringResource(R.string.title_support)) { SupportSettingsItems() }
      SettingsCategories(title = stringResource(R.string.title_opensource)) { DevelopmentSettingsItems() }
    }
  }
}

@Composable
fun SettingsCategories(title: String? = null, settingsItems: @Composable () -> Unit = {}) {
  Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
    if (title != null)
      Text(
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
      modifier =
      Modifier
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
            style = MaterialTheme.typography.titleMedium
          )
          if (subtitle != null) Text(text = subtitle, color = WalletColors.styleguide_dark_grey)
        }
      }
      content()
    }
  }
}

@Composable
fun SettingsItems() {
  SettingsItem(
    icon = painterResource(R.drawable.ic_manage_wallet),
    title = stringResource(R.string.manage_wallet_button),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_manage_subscriptions),
    title = stringResource(R.string.subscriptions_settings_title),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_currency),
    title = stringResource(R.string.change_currency_settings_title),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_settings_fingerprint),
    title = stringResource(R.string.fingerprint_settings),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_updates),
    title = stringResource(R.string.check_updates_settings_title),
    action = {})
}

@Composable
fun CommunitySettingsItems() {
  SettingsItem(
    icon = painterResource(R.drawable.ic_twitter),
    title = stringResource(R.string.twitter),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_facebook),
    title = stringResource(R.string.facebook),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_telegram),
    title = stringResource(R.string.telegram),
    action = {})
}

@Composable
fun SupportSettingsItems() {
  SettingsItem(
    icon = painterResource(R.drawable.ic_faqs),
    title = stringResource(R.string.faq),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_support),
    title = stringResource(R.string.settings_item_support),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_privacy_policy),
    title = stringResource(R.string.title_privacy_policy),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_terms_conditions),
    title = stringResource(R.string.title_terms),
    action = {})
}

@Composable
fun DevelopmentSettingsItems() {
  SettingsItem(
    icon = painterResource(R.drawable.ic_open_source),
    title = stringResource(R.string.title_source_code),
    action = {})
  SettingsItem(
    icon = painterResource(R.drawable.ic_heart),
    title = stringResource(R.string.credits),
    action = {})
}

@Preview
@Composable
fun PreviewSettingsCategories() {
  SettingsCategories("Title") {
    SettingsItem(
      icon = painterResource(R.drawable.ic_manage_wallet),
      title = stringResource(R.string.manage_wallet_button),
      action = {})
    SettingsItem(
      icon = painterResource(R.drawable.ic_manage_wallet),
      title = stringResource(R.string.manage_wallet_button),
      action = {})
  }
}

@Preview
@Composable
fun PreviewSettingsItem() {
  SettingsItem(painterResource(id = R.drawable.ic_settings_fingerprint), "Title", "subtitle", {}) {
    Text(text = "Content")
  }
}
