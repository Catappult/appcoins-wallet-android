package com.asfoundation.wallet.manage_wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.widgets.BalanceItem
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TotalBalance
import com.appcoins.wallet.ui.widgets.VectorIconButton
import com.appcoins.wallet.ui.widgets.component.BottomSheetButton
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asf.wallet.R
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageWalletFragment : BasePageViewFragment() {
  private val viewModel: ManageWalletViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { ManageWalletView() } }
  }

  @Composable
  fun ManageWalletView() {
    Scaffold(
      topBar = {
        Surface(shadowElevation = 4.dp) {
          TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() })
        }
      },
      containerColor = styleguide_blue
    ) { padding ->
      ManageWalletContent(padding = padding)
    }
  }

  @Composable
  internal fun ManageWalletContent(padding: PaddingValues) {
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(padding),
    ) {
      ScreenHeader()
      when (val uiState = viewModel.uiState.collectAsState().value) {
        is ManageWalletViewModel.UiState.Balance -> BalanceBottomSheet(uiState.balance)
        else -> {}
      }
    }
  }

  @Composable
  fun ScreenHeader() {
    Row(
      horizontalArrangement = SpaceBetween,
      verticalAlignment = CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      ScreenTitle()
      ManagementOptionsBottomSheet()
    }
  }

  @Composable
  fun ScreenTitle() {
    Text(
      text = stringResource(R.string.manage_wallet_button),
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = WalletColors.styleguide_light_grey,
    )
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun ManagementOptionsBottomSheet() {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState =
      rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    Row(
      horizontalArrangement = Arrangement.End,
      modifier = Modifier
        .padding(end = 16.dp)
        .fillMaxSize()
    ) {
      VectorIconButton(
        imageVector = Icons.Default.MoreVert,
        contentDescription = R.string.action_more_details,
        onClick = { openBottomSheet = !openBottomSheet })
    }

    if (openBottomSheet) {
      ModalBottomSheet(
        onDismissRequest = { openBottomSheet = false },
        sheetState = bottomSheetState,
        containerColor = styleguide_blue_secondary
      ) {
        Column(
          Modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp),
          verticalArrangement = Arrangement.Center
        ) {
          BottomSheetButton(
            R.drawable.ic_plus_v3,
            R.string.my_wallets_action_new_wallet,
            onClick = {
              scope
                .launch { bottomSheetState.hide() }
                .invokeOnCompletion { /*navigator.navigateToCreateNewWallet()*/ }
            })
          BottomSheetButton(
            R.drawable.ic_recover_wallet,
            R.string.my_wallets_action_recover_wallet,
            onClick = {
              scope
                .launch { bottomSheetState.hide() }
                .invokeOnCompletion { /*navigator.navigateToRestoreWallet()*/ }
            })
          BottomSheetButton(
            R.drawable.ic_delete_v3,
            R.string.my_wallets_action_delete_wallet,
            onClick = {
              scope
                .launch { bottomSheetState.hide() }
                .invokeOnCompletion { /*navigator.navigateToRemoveWallet()*/ }
            })
        }
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun BalanceBottomSheet(balance: WalletBalance) {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val bottomSheetState =
      rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = CenterVertically,
      modifier = Modifier
        .padding(start = 16.dp)
        .fillMaxWidth()
    ) {
      Text(
        "Melissa Wallet", // TODO
        color = WalletColors.styleguide_light_grey, style = MaterialTheme.typography.bodySmall
      )
      ButtonWithText(
        label = balance.creditsOnlyFiat.amount.toString()
          .formatMoney(balance.creditsOnlyFiat.symbol, "") ?: "",
        onClick = { openBottomSheet = !openBottomSheet },
        labelColor = WalletColors.styleguide_light_grey
      )
    }

    if (openBottomSheet) {
      ModalBottomSheet(
        onDismissRequest = { openBottomSheet = false },
        sheetState = bottomSheetState,
        containerColor = styleguide_blue_secondary
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 48.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          TotalBalance(
            amount = balance.creditsOnlyFiat.amount.toString()
              .formatMoney(balance.creditsOnlyFiat.symbol, "") ?: "",
            convertedAmount = "${
              balance.creditsBalance.token.amount.toString().formatMoney()
            } ${balance.creditsBalance.token.symbol}"
          )
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            BalanceItem(
              icon = R.drawable.ic_appc_token,
              currencyName = R.string.appc_token_name,
              balance = "${
                balance.appcBalance.token.amount.toString().formatMoney()
              } ${balance.appcBalance.token.symbol}"
            )
            BalanceItem(
              icon = R.drawable.ic_appc_c_token,
              currencyName = R.string.appc_credits_token_name,
              balance = "${
                balance.creditsBalance.token.amount.toString().formatMoney()
              } ${balance.creditsBalance.token.symbol}"
            )
            BalanceItem(
              icon = R.drawable.ic_eth_token,
              currencyName = R.string.ethereum_token_name,
              balance = "${
                balance.ethBalance.token.amount.toString().formatMoney()
              } ${balance.ethBalance.token.symbol}"
            )
          }
        }
      }
    }
  }
}
