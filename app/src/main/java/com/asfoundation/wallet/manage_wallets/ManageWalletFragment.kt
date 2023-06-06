package com.asfoundation.wallet.manage_wallets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.fragment.app.viewModels
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.core.utils.android_common.extensions.StringUtils.masked
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
import com.appcoins.wallet.ui.widgets.BackupAlertCard
import com.appcoins.wallet.ui.widgets.BalanceItem
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TotalBalance
import com.appcoins.wallet.ui.widgets.VectorIconButton
import com.appcoins.wallet.ui.widgets.VerifyWalletAlertCard
import com.appcoins.wallet.ui.widgets.component.BottomSheetButton
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletBottomSheet
import com.appcoins.wallet.ui.widgets.component.WalletTextField
import com.asf.wallet.R
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel.UiState.Loading
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel.UiState.Success
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel.UiState.WalletChanged
import com.asfoundation.wallet.my_wallets.main.MyWalletsNavigator
import com.asfoundation.wallet.my_wallets.more.MoreDialogNavigator
import com.asfoundation.wallet.transactions.TransactionDetailsFragment
import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.balance.TokenBalance
import com.asfoundation.wallet.ui.wallets.WalletBalance
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.wallets.domain.WalletInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class ManageWalletFragment : BasePageViewFragment() {

  @Inject
  lateinit var navigator: MoreDialogNavigator

  @Inject
  lateinit var myWalletsNavigator: MyWalletsNavigator

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
        Surface { TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() }) }
      },
      containerColor = styleguide_blue,
    ) { padding ->
      when (val uiState = viewModel.uiState.collectAsState().value) {
        is Success ->
          ManageWalletContent(
            padding = padding, uiState.activeWalletInfo, uiState.inactiveWallets
          )

        WalletChanged -> {
          Toast.makeText(context, "Wallet changed", Toast.LENGTH_SHORT).show()
        }

        Loading -> Row(
          modifier = Modifier.fillMaxSize(),
          verticalAlignment = CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          CircularProgressIndicator()
        }

        else -> {}
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  internal fun ManageWalletContent(
    padding: PaddingValues,
    walletInfo: WalletInfo,
    inactiveWallets: List<WalletBalance>
  ) {
    LazyColumn(modifier = Modifier.padding(padding)) {
      item { ScreenHeader(walletInfo) }
      item { ActiveWalletCard(walletInfo) }

      items(inactiveWallets) { wallet ->
        Card(
          colors = CardDefaults.cardColors(styleguide_blue_secondary),
          modifier = Modifier
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp),
          onClick = {
            viewModel.inactiveWalletBalance.value = wallet
            viewModel.openBottomSheet.value = !viewModel.openBottomSheet.value
          }) {
          InactiveWalletCard(wallet)
        }
      }
    }
    ChangeWalletBottomSheet(viewModel.inactiveWalletBalance.value)
  }

  @Composable
  fun ActiveWalletCard(walletInfo: WalletInfo) {
    Column(horizontalAlignment = End, modifier = Modifier.padding(16.dp)) {
      ActiveWalletIndicator()
      Card(
        colors = CardDefaults.cardColors(styleguide_blue_secondary),
        border = BorderStroke(1.dp, styleguide_pink),
        shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp, topStart = 16.dp)
      ) {
        Column(
          modifier =
          Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp)
        ) {
          BalanceBottomSheet(walletInfo)
          ActiveWalletOptions(walletInfo.wallet)
          Spacer(modifier = Modifier.height(24.dp))
          BackupAlertCard(
            onClickButton = {
              myWalletsNavigator.navigateToBackupWallet(walletInfo.wallet)
            },
            hasBackup = walletInfo.hasBackup,
            backupDate = walletInfo.backupDate
          )
          Separator()
          VerifyWalletAlertCard(
            onClickButton = { myWalletsNavigator.navigateToVerifyCreditCard() },
            verified = walletInfo.verified
          )
        }
      }
    }
  }

  @Composable
  fun ActiveWalletIndicator() {
    Surface(color = styleguide_pink, shape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp)) {
      Text(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        text = stringResource(R.string.wallets_active_wallet_title),
        color = styleguide_blue,
        style = MaterialTheme.typography.bodySmall
      )
    }
  }

  @Composable
  fun Separator() {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 24.dp)
        .height(1.dp),
      color = styleguide_blue,
      content = {})
  }

  @Composable
  fun ActiveWalletOptions(wallet: String) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = stringResource(R.string.transfer_send_recipient_title),
          style = MaterialTheme.typography.bodySmall,
          color = WalletColors.styleguide_dark_grey
        )
        Text(
          text = wallet.masked(),
          style = MaterialTheme.typography.bodySmall,
          color = styleguide_light_grey
        )
      }
      Row {
        EditNameBottomSheet(wallet)
        VectorIconButton(painter = painterResource(R.drawable.ic_qrcode),
          contentDescription = R.string.scan_qr,
          onClick = {})
        VectorIconButton(
          imageVector = Icons.Default.Share,
          contentDescription = R.string.wallet_view_share_button,
          onClick = { shareAddress(wallet) })
        VectorIconButton(
          painter = painterResource(R.drawable.ic_copy_to_clip),
          contentDescription = R.string.wallet_view_copy_button,
          onClick = { copyAddressToClipBoard(wallet) })
      }
    }
  }

  @Composable
  fun ScreenHeader(walletInfo: WalletInfo) {
    Row(
      horizontalArrangement = SpaceBetween,
      verticalAlignment = CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      ScreenTitle()
      ManagementOptionsBottomSheet(walletInfo)
    }
  }

  @Composable
  fun ScreenTitle() {
    Text(
      text = stringResource(R.string.manage_wallet_button),
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = styleguide_light_grey,
    )
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun ManagementOptionsBottomSheet(walletInfo: WalletInfo) {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(false)

    Row(
      horizontalArrangement = Arrangement.End,
      modifier = Modifier
        .padding(end = 16.dp)
        .fillMaxSize()
    ) {
      VectorIconButton(
        imageVector = Icons.Default.MoreVert,
        contentDescription = R.string.action_more_details,
        onClick = { openBottomSheet = !openBottomSheet },
        paddingIcon = 4.dp,
        background = styleguide_blue_secondary
      )
    }

    WalletBottomSheet(
      openBottomSheet, onDismissRequest = { openBottomSheet = false }, bottomSheetState
    ) {
      Column(verticalArrangement = Arrangement.Center) {
        BottomSheetButton(R.drawable.ic_plus_v3, R.string.my_wallets_action_new_wallet, onClick = {
          scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
            openBottomSheet = !openBottomSheet
            navigator.navigateToCreateNewWallet()
          }
        })
        BottomSheetButton(R.drawable.ic_recover_wallet,
          R.string.my_wallets_action_recover_wallet,
          onClick = {
            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
              openBottomSheet = !openBottomSheet
              navigator.navigateToRestoreWallet()
            }
          })
        BottomSheetButton(R.drawable.ic_delete_v3,
          R.string.my_wallets_action_delete_wallet,
          onClick = {
            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
              openBottomSheet = !openBottomSheet
              navigator.navigateToRemoveWallet(
                walletAddress = walletInfo.wallet,
                totalFiatBalance = walletInfo.walletBalance.overallFiat.amount.toString(),
                appcoinsBalance = walletInfo.walletBalance.appcBalance.token.amount.toString(),
                creditsBalance = walletInfo.walletBalance.creditsBalance.token.amount.toString(),
                ethereumBalance = walletInfo.walletBalance.ethBalance.token.amount.toString()
              )
            }
          })
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun BalanceBottomSheet(walletInfo: WalletInfo) {
    val balance = walletInfo.walletBalance
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }

    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = walletInfo.name,
        modifier = Modifier.fillMaxWidth(0.5f),
        color = styleguide_light_grey,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      TextButton(onClick = { openBottomSheet = !openBottomSheet }) {
        Text(
          text =
          balance.creditsOnlyFiat.amount
            .toString()
            .formatMoney(balance.creditsOnlyFiat.symbol, "") ?: "",
          style = MaterialTheme.typography.bodyMedium,
          color = styleguide_light_grey,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    WalletBottomSheet(openBottomSheet, { openBottomSheet = false }) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TotalBalance(
          amount = balance.creditsOnlyFiat.amount.toString()
            .formatMoney(balance.creditsOnlyFiat.symbol, "") ?: "", convertedAmount = "${
            balance.creditsBalance.token.amount.toString().formatMoney()
          } ${balance.creditsBalance.token.symbol}"
        )
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          BalanceItem(
            icon = R.drawable.ic_appc_token, currencyName = R.string.appc_token_name, balance = "${
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

  @Composable
  fun InactiveWalletCard(walletBalance: WalletBalance) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
      Text(
        text = walletBalance.walletName,
        modifier = Modifier.fillMaxWidth(0.5f),
        color = WalletColors.styleguide_dark_grey,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text =
        walletBalance.balance.amount
          .toString()
          .formatMoney(walletBalance.balance.symbol, "")
          ?: "",
        style = MaterialTheme.typography.bodyMedium,
        color = WalletColors.styleguide_dark_grey,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun ChangeWalletBottomSheet(walletBalance: WalletBalance) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(false)

    WalletBottomSheet(
      viewModel.openBottomSheet.value, { viewModel.openBottomSheet.value = false }, bottomSheetState
    ) {
      Column(verticalArrangement = Arrangement.Center) {
        Text(
          modifier = Modifier.padding(bottom = 24.dp),
          text = "Do you want to select this wallet?",
          color = styleguide_light_grey,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold
        )
        Card(
          colors = CardDefaults.cardColors(styleguide_blue),
          modifier = Modifier.padding(bottom = 24.dp)
        ) {
          InactiveWalletCard(walletBalance)
        }
        ButtonWithText(
          label = stringResource(R.string.wallet_view_activate_button),
          labelColor = styleguide_light_grey,
          backgroundColor = styleguide_pink,
          onClick = {
            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
              viewModel.openBottomSheet.value = !viewModel.openBottomSheet.value
              viewModel.changeActiveWallet(walletBalance.walletAddress)
            }
          },
          buttonType = ButtonType.LARGE
        )
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun EditNameBottomSheet(wallet: String) {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(false)
    var name by remember { mutableStateOf("") }

    VectorIconButton(imageVector = Icons.Default.Edit,
      contentDescription = R.string.action_edit,
      onClick = { openBottomSheet = !openBottomSheet })

    WalletBottomSheet(openBottomSheet, { openBottomSheet = false }, bottomSheetState) {
      LazyColumn(verticalArrangement = Arrangement.Center) {
        item {
          Text(
            modifier = Modifier.padding(bottom = 24.dp),
            text = "Change name of your wallet",
            color = styleguide_light_grey,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
          )
        }

        item {
          WalletTextField(name) { newName ->
            name = newName
          }
        }

        item {
          ButtonWithText(
            label = stringResource(R.string.action_save),
            labelColor = styleguide_light_grey,
            backgroundColor = styleguide_pink,
            onClick = {
              scope.launch {
                bottomSheetState.hide()
              }.invokeOnCompletion {
                openBottomSheet = !openBottomSheet
                viewModel.setName(wallet, name)
              }
            },
            buttonType = ButtonType.LARGE
          )
        }
      }
    }
  }

  private fun copyAddressToClipBoard(address: String) {
    val clipboard =
      requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(TransactionDetailsFragment.ORDER_KEY, address)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
  }

  private fun shareAddress(walletAddress: String) =
    ShareCompat.IntentBuilder(requireActivity())
      .setText(walletAddress)
      .setType("text/plain")
      .setChooserTitle(resources.getString(R.string.share_via))
      .startChooser()

  @Preview
  @Composable
  fun PreviewActiveWalletOptions() {
    ActiveWalletOptions("a24863cb-e586-472f-9e8a-622834c20c52")
  }

  @Preview
  @Composable
  fun PreviewHeader() {
    val fiatValue = FiatValue(amount = BigDecimal(123456), "EUR", "€")
    val tokenBalance = TokenBalance(TokenValue(BigDecimal.TEN, "EUR"), fiatValue)
    BalanceBottomSheet(
      walletInfo =
      WalletInfo(
        "a24863cb-e586-472f-9e8a-622834c20c52",
        "a24863cb-e586-472f-9e8a-622834c20c52a24863cb-e586-472f-9e8a-622834c20c52",
        com.asfoundation.wallet.wallets.domain.WalletBalance(
          fiatValue, fiatValue, tokenBalance, tokenBalance, tokenBalance
        ),
        blocked = false,
        verified = true,
        logging = false,
        backupDate = 987654L
      )
    )
  }

  @Preview
  @Composable
  fun PreviewInactiveWallet() {
    val fiatValue = FiatValue(amount = BigDecimal(123456), "EUR", "€")
    InactiveWalletCard(
      WalletBalance(
        walletName = "a24863cb-e586-472f-9e8a-62283420c52",
        walletAddress = "a24863cb-e586-472f-9e8a-622834c20c52a24863cb-e586-472f-9e8a-622834c20c52",
        balance = fiatValue,
        isActiveWallet = true,
        backupDate = 987654L
      )
    )
  }
}
