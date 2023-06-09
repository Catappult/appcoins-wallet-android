package com.asfoundation.wallet.manage_wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asf.wallet.R
import com.asfoundation.wallet.my_wallets.main.MyWalletsNavigator
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class RemoveWalletFragment : BasePageViewFragment() {

  private val viewModel: ManageWalletViewModel by viewModels()

  @Inject
  lateinit var myWalletsNavigator: MyWalletsNavigator

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { RemoveWalletView() } }
  }

  @Composable
  fun RemoveWalletView() {
    Scaffold(
      topBar = {
        Surface { TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() }) }
      },
      containerColor = WalletColors.styleguide_blue,
    ) { padding ->
      when (val uiState = viewModel.uiState.collectAsState().value) {
        is ManageWalletViewModel.UiState.Success ->
          RemoveWalletContent(padding = padding, uiState.activeWalletInfo)

        ManageWalletViewModel.UiState.Loading ->
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Center
          ) {
            CircularProgressIndicator()
          }

        ManageWalletViewModel.UiState.WalletDeleted -> navigateToManageWallet()

        else -> {}
      }
    }
  }

  @Composable
  internal fun RemoveWalletContent(padding: PaddingValues, walletInfo: WalletInfo) {
    LazyColumn(
      modifier = Modifier
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      item { ScreenTitle() }

      item { ScreenSubtitle() }

      item { BalanceCard(walletInfo) }

      item { AlertCard() }

      item { ActionButtons(walletInfo.wallet) }
    }
  }

  @Composable
  fun ScreenTitle() {
    Text(
      text = stringResource(R.string.remove_wallet_title),
      modifier = Modifier.padding(8.dp),
      style = typography.headlineSmall,
      fontWeight = Bold,
      color = styleguide_light_grey,
    )
  }

  @Composable
  fun ScreenSubtitle() {
    Text(
      text = stringResource(R.string.title_delete_account),
      modifier = Modifier.padding(8.dp),
      style = typography.bodyMedium,
      fontWeight = Medium,
      color = styleguide_light_grey,
    )
  }

  @Composable
  fun BalanceCard(walletInfo: WalletInfo) {
    with(walletInfo.walletBalance) {
      Card(
        modifier = Modifier.padding(vertical = 24.dp),
        colors =
        CardDefaults.cardColors(
          containerColor =
          WalletColors.styleguide_blue_secondary
        )
      ) {
        Column(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
        ) {
          BalanceInfo(
            description = walletInfo.wallet,
            amount = creditsOnlyFiat.amount,
            currency = creditsOnlyFiat.currency,
            name = walletInfo.name
          )
          Spacer(Modifier.size(8.dp))
          BalanceInfo(
            stringResource(R.string.appc_token_name),
            amount = appcBalance.token.amount,
            currency = appcBalance.token.symbol
          )
          BalanceInfo(
            stringResource(R.string.appc_credits_token_name),
            amount = creditsBalance.token.amount,
            currency = creditsBalance.token.symbol
          )
          BalanceInfo(
            stringResource(R.string.appc_credits_token_name),
            amount = ethBalance.token.amount,
            currency = ethBalance.token.symbol
          )
        }

      }
    }
  }

  @Composable
  fun BalanceInfo(description: String, amount: BigDecimal, currency: String, name: String? = null) {
    Row(
      horizontalArrangement = SpaceBetween,
      verticalAlignment = CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp)
    ) {
      Column(verticalArrangement = Center, modifier = Modifier.fillMaxWidth(0.6f)) {
        if (name != null)
          Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = name,
            color = styleguide_light_grey,
            style = typography.bodyMedium,
            fontWeight = Medium,
            maxLines = 1,
            overflow = Ellipsis,
          )
        Text(
          text = description,
          color = WalletColors.styleguide_dark_grey,
          style = typography.bodySmall,
          maxLines = 1,
          overflow = Ellipsis,
        )
      }

      Text(
        text = "${amount.toString().formatMoney()} $currency",
        style = if (name != null) typography.bodyMedium else typography.bodySmall,
        color = styleguide_light_grey,
        fontWeight = if (name != null) Bold else Medium,
        maxLines = 1,
        overflow = Ellipsis
      )
    }
  }

  @Composable
  fun AlertCard() {
    Card(
      colors = CardDefaults.cardColors(containerColor = WalletColors.styleguide_blue),
      border = BorderStroke(1.dp, styleguide_pink),
      modifier = Modifier.padding(horizontal = 16.dp)
    ) {
      Column(
        verticalArrangement = Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
      ) {
        Image(
          painterResource(R.drawable.ic_alert_circle),
          contentDescription = null,
          modifier = Modifier.size(48.dp)
        )
        Text(
          modifier = Modifier.padding(top = 24.dp),
          text = stringResource(id = R.string.remove_wallet_body),
          style = typography.bodySmall,
          color = styleguide_light_grey,
          textAlign = TextAlign.Center,
          fontWeight = Medium
        )
      }
    }
  }

  @Composable
  fun ActionButtons(address: String) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier
        .padding(top = 48.dp)
        .padding(horizontal = 8.dp)
    ) {
      ButtonWithText(
        label = stringResource(id = R.string.my_wallets_action_backup_wallet),
        onClick = { myWalletsNavigator.navigateToBackupWallet(address) },
        labelColor = styleguide_light_grey,
        backgroundColor = styleguide_pink,
        buttonType = ButtonType.LARGE
      )

      ButtonWithText(
        label = stringResource(id = R.string.my_wallets_action_delete_wallet),
        onClick = { viewModel.deleteWallet(address) },
        labelColor = styleguide_light_grey,
        outlineColor = styleguide_light_grey,
        buttonType = ButtonType.LARGE
      )
    }
  }

  private fun navigateToManageWallet() {
    setFragmentResult(ManageWalletFragment.MANAGE_WALLET_REQUEST_KEY, bundleOf())
    requireActivity().onBackPressedDispatcher.onBackPressed()
  }
}
