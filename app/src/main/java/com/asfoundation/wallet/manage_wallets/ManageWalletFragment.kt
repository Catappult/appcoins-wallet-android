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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.core.utils.android_common.extensions.StringUtils.masked
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.walletInfo.data.balance.TokenBalance
import com.appcoins.wallet.feature.walletInfo.data.balance.TokenValue
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletInfoSimple
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.CODE_REQUESTED
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.VERIFIED
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatus.VERIFYING
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationStatusCompound
import com.appcoins.wallet.feature.walletInfo.data.verification.VerificationType
import com.appcoins.wallet.feature.walletInfo.data.wallet.domain.WalletInfo
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
import com.appcoins.wallet.ui.widgets.BackupAlertCard
import com.appcoins.wallet.ui.widgets.ScreenTitle
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.VectorIconButton
import com.appcoins.wallet.ui.widgets.VerifyWalletAlertCard
import com.appcoins.wallet.ui.widgets.component.Animation
import com.appcoins.wallet.ui.widgets.expanded
import com.asf.wallet.R
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel.UiState.Loading
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel.UiState.Success
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel.UiState.WalletChanged
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel.UiState.WalletCreated
import com.asfoundation.wallet.manage_wallets.ManageWalletViewModel.UiState.WalletDeleted
import com.asfoundation.wallet.manage_wallets.bottom_sheet.ManageWalletSharedViewModel
import com.asfoundation.wallet.my_wallets.main.MyWalletsNavigator
import com.asfoundation.wallet.ui.bottom_navigation.TransferDestinations
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class ManageWalletFragment : BasePageViewFragment() {

  @Inject
  lateinit var myWalletsNavigator: MyWalletsNavigator

  @Inject
  lateinit var analytics: ManageWalletAnalytics

  private val viewModel: ManageWalletViewModel by viewModels()

  private val manageWalletSharedViewModel: ManageWalletSharedViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { ManageWalletView() } }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setFragmentResultListener(MANAGE_WALLET_REQUEST_KEY) { _, _ -> viewModel.updateWallets() }
  }

  override fun onResume() {
    super.onResume()
    viewModel.getWallets()
  }

  @Composable
  fun ManageWalletView() {
    val dialogDismissed by manageWalletSharedViewModel.dialogDismissed
    LaunchedEffect(key1 = dialogDismissed) { viewModel.getWallets() }
    Scaffold(
      topBar = {
        Surface { TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() }) }
      },
      containerColor = styleguide_blue,
    ) { padding ->
      when (val uiState = viewModel.uiState.collectAsState().value) {
        is Success -> {
          ManageWalletContent(
            padding = padding,
            uiState.activeWalletInfo,
            uiState.inactiveWallets,
            uiState.verificationStatus
          )
        }

        WalletChanged -> {
          Toast.makeText(context, R.string.manage_wallet_wallet_changed_title, Toast.LENGTH_SHORT)
            .show()
        }

        WalletCreated -> {
          Toast.makeText(context, R.string.intro_wallet_created_short, Toast.LENGTH_SHORT).show()
        }

        Loading ->
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
          }

        WalletDeleted -> viewModel.updateWallets()
        else -> {}
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  internal fun ManageWalletContent(
    padding: PaddingValues,
    walletInfo: WalletInfo,
    inactiveWallets: List<WalletInfoSimple>,
    verificationStatus: VerificationStatusCompound
  ) {
    LazyColumn(modifier = Modifier.padding(padding)) {
      item { ScreenHeader(inactiveWallets.size) }
      item { ActiveWalletCard(walletInfo, verificationStatus) }

      items(inactiveWallets) { wallet ->
        Card(
          colors = CardDefaults.cardColors(styleguide_blue_secondary),
          modifier = Modifier
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp),
          onClick = {
            myWalletsNavigator.navigateToChangeActiveWalletBottomSheet(
              wallet.walletAddress,
              wallet.walletName,
              wallet.balance.amount.toString(),
              wallet.balance.symbol
            )
          }) {
          InactiveWalletCard(wallet)
        }
      }
    }
  }

  @Composable
  fun ActiveWalletCard(walletInfo: WalletInfo, verificationStatus: VerificationStatusCompound) {
    Column(horizontalAlignment = End, modifier = Modifier.padding(16.dp)) {
      ActiveWalletIndicator()
      Card(
        colors = CardDefaults.cardColors(styleguide_blue_secondary),
        border = BorderStroke(1.dp, styleguide_pink),
        shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp, topStart = 16.dp)
      ) {
        BoxWithConstraints {
          if (expanded())
            ActiveWalletContentLandscape(
              walletInfo = walletInfo,
              verificationStatus = verificationStatus
            )
          else
            ActiveWalletContentPortrait(
              walletInfo = walletInfo,
              verificationStatus = verificationStatus
            )
        }
      }
    }
  }

  @Composable
  fun ActiveWalletContentPortrait(
    walletInfo: WalletInfo,
    verificationStatus: VerificationStatusCompound
  ) {
    Column(
      modifier =
      Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp)
    ) {
      BalanceBottomSheet(walletInfo)
      ActiveWalletOptions(walletInfo.wallet, walletInfo.name)
      Spacer(modifier = Modifier.height(24.dp))
      BackupAlertCard(
        onClickButton = {
          myWalletsNavigator.navigateToBackup(walletInfo.wallet, walletInfo.name)
        },
        hasBackup = walletInfo.hasBackup,
        backupDate = walletInfo.backupDate
      )
      Separator()
      if (
        isVerificationInProcessing(verificationStatus.creditCardStatus, walletInfo.verified) ||
        isVerificationInProcessing(verificationStatus.payPalStatus, walletInfo.verified)
      ) {
        LoadingCard()
      } else {
        VerifyWalletAlertCard(
          onClickButton = {
            analytics.sendManageWalletScreenEvent(action = VERIFY_PAYMENT_METHOD)
            when {
              (verificationStatus.creditCardStatus == VERIFYING ||
                  verificationStatus.creditCardStatus == CODE_REQUESTED) &&
                  verificationStatus.currentVerificationType == VerificationType.CREDIT_CARD ->
                myWalletsNavigator.navigateToCCVerification()

              (verificationStatus.payPalStatus == VERIFYING ||
                  verificationStatus.payPalStatus == CODE_REQUESTED) &&
              verificationStatus.currentVerificationType == VerificationType.PAYPAL ->
                myWalletsNavigator.navigateToPPVerification()

              else -> myWalletsNavigator.navigateToVerifyPicker()
            }
          },
          verifiedCC = walletInfo.verified && verificationStatus.creditCardStatus == VERIFIED,
          verifiedPP = walletInfo.verified && verificationStatus.payPalStatus == VERIFIED,
          verifiedWeb = walletInfo.verified,
          waitingCodeCC = (verificationStatus.creditCardStatus == VERIFYING ||
              verificationStatus.creditCardStatus == CODE_REQUESTED) &&
              verificationStatus.currentVerificationType == VerificationType.CREDIT_CARD,
          waitingCodePP = (verificationStatus.payPalStatus == VERIFYING ||
              verificationStatus.payPalStatus == CODE_REQUESTED) &&
              verificationStatus.currentVerificationType == VerificationType.PAYPAL,
          onCancelClickButton = {
            viewModel.cancelVerification(walletInfo.wallet)
            viewModel.updateWallets()
          }
        )
      }
    }
  }

  @Composable
  fun ActiveWalletContentLandscape(
    walletInfo: WalletInfo,
    verificationStatus: VerificationStatusCompound
  ) {
    Column(
      modifier =
      Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
      Row(verticalAlignment = CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        BalanceBottomSheet(walletInfo, modifier = Modifier.weight(1f))
        Spacer(Modifier.weight(0.05f))
        ActiveWalletOptions(walletInfo.wallet, walletInfo.name, Modifier.weight(1f))
      }

      Spacer(modifier = Modifier.height(24.dp))
      Row(verticalAlignment = CenterVertically) {
        BackupAlertCard(
          onClickButton = {
            myWalletsNavigator.navigateToBackup(walletInfo.wallet, walletInfo.name)
          },
          hasBackup = walletInfo.hasBackup,
          backupDate = walletInfo.backupDate,
          modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.weight(0.05f))
        if (
          isVerificationInProcessing(verificationStatus.creditCardStatus, walletInfo.verified) ||
          isVerificationInProcessing(verificationStatus.payPalStatus, walletInfo.verified)
        ) {
          LoadingCard(modifier = Modifier.weight(1f))
        } else {
          VerifyWalletAlertCard(
            onClickButton = {
              analytics.sendManageWalletScreenEvent(action = VERIFY_PAYMENT_METHOD)
              when {
                (verificationStatus.creditCardStatus == VERIFYING ||
                    verificationStatus.creditCardStatus == CODE_REQUESTED) &&
                    verificationStatus.currentVerificationType == VerificationType.CREDIT_CARD ->
                  myWalletsNavigator.navigateToCCVerification()

                (verificationStatus.payPalStatus == VERIFYING ||
                    verificationStatus.payPalStatus == CODE_REQUESTED) &&
                    verificationStatus.currentVerificationType == VerificationType.PAYPAL ->
                  myWalletsNavigator.navigateToPPVerification()

                else -> myWalletsNavigator.navigateToVerifyPicker()
              }
            },
            verifiedCC = walletInfo.verified && verificationStatus.creditCardStatus == VERIFIED,
            verifiedPP = walletInfo.verified && verificationStatus.payPalStatus == VERIFIED,
            verifiedWeb = walletInfo.verified,
            waitingCodeCC = verificationStatus.creditCardStatus == VERIFYING ||
                verificationStatus.creditCardStatus == CODE_REQUESTED,
            waitingCodePP = verificationStatus.payPalStatus == VERIFYING ||
                verificationStatus.payPalStatus == CODE_REQUESTED,
            onCancelClickButton = {
              viewModel.cancelVerification(walletInfo.wallet)
              viewModel.updateWallets()
            },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }
  }

  @Preview
  @Composable
  private fun LoadingCard(modifier: Modifier = Modifier) {
    Column(
      modifier = modifier
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Animation(modifier = Modifier.size(104.dp), animationRes = R.raw.loading_wallet)
    }
  }

  private fun isVerificationInProcessing(
    verificationStatus: VerificationStatus,
    isVerified: Boolean
  ) =
    verificationStatus == VERIFIED && !isVerified

  @Composable
  fun ActiveWalletIndicator() {
    Surface(color = styleguide_pink, shape = RoundedCornerShape(topEnd = 8.dp, topStart = 8.dp)) {
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
  fun ActiveWalletOptions(wallet: String, walletName: String, modifier: Modifier = Modifier) {
    Row(
      modifier = modifier.fillMaxWidth(),
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
        VectorIconButton(
          imageVector = Icons.Default.Edit,
          contentDescription = R.string.action_edit,
          onClick = {
            myWalletsNavigator.navigateToManageWalletNameBottomSheet(wallet, walletName)
          })
        VectorIconButton(
          painter = painterResource(R.drawable.ic_qrcode),
          contentDescription = R.string.scan_qr,
          onClick = {
            myWalletsNavigator.navigateToReceive(
              navController(), TransferDestinations.RECEIVE
            )
          })
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
  fun ScreenHeader(inactiveWalletsQuantity: Int) {
    Row(
      horizontalArrangement = SpaceBetween,
      verticalAlignment = CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      ScreenTitle(stringResource(R.string.manage_wallet_view_title))
      ManagementOptionsBottomSheet(inactiveWalletsQuantity)
    }
  }

  @Composable
  fun ManagementOptionsBottomSheet(inactiveWalletsQuantity: Int) {
    Row(
      horizontalArrangement = Arrangement.End,
      modifier = Modifier
        .padding(end = 16.dp)
        .fillMaxSize()
    ) {
      VectorIconButton(
        imageVector = Icons.Default.MoreVert,
        contentDescription = R.string.action_more_details,
        onClick = {
          myWalletsNavigator.navigateToManageWalletBottomSheet(inactiveWalletsQuantity == 0)
        },
        paddingIcon = 4.dp,
        background = styleguide_blue_secondary
      )
    }
  }

  @Composable
  fun BalanceBottomSheet(walletInfo: WalletInfo, modifier: Modifier = Modifier) {
    val balance = walletInfo.walletBalance

    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = CenterVertically,
      modifier = modifier.fillMaxWidth()
    ) {
      Text(
        text = walletInfo.name,
        modifier = Modifier.fillMaxWidth(0.5f),
        color = styleguide_light_grey,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      TextButton(
        onClick = {
          myWalletsNavigator.navigateToManageWalletBalanceBottomSheet(
            walletInfo.walletBalance
          )
        }) {
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
  }

  @Composable
  fun InactiveWalletCard(walletBalance: WalletInfoSimple) {
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
          .formatMoney(walletBalance.balance.symbol, "") ?: "",
        style = MaterialTheme.typography.bodyMedium,
        color = WalletColors.styleguide_dark_grey,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

  private fun copyAddressToClipBoard(address: String) {
    val clipboard =
      requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(ADDRESS_KEY, address)
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
    ActiveWalletOptions("a24863cb-e586-472f-9e8a-622834c20c52", "Wallet test")
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
        WalletBalance(fiatValue, fiatValue, tokenBalance, tokenBalance, tokenBalance),
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
      WalletInfoSimple(
        walletName = "a24863cb-e586-472f-9e8a-622834c20c52",
        walletAddress =
        "a24863cb-e586-472f-9e8a-622834c20c52a24863cb-e586-472f-9e8a-622834c20c52",
        balance = fiatValue,
        isActiveWallet = true,
        backupDate = 987654L,
        backupWalletActive = false
      )
    )
  }


  @Preview(widthDp = 601)
  @Composable
  fun PreviewActiveWalletCardLandscape() {
    val fiatValue = FiatValue(amount = BigDecimal(123456), "EUR", "€")
    val tokenBalance = TokenBalance(TokenValue(amount = BigDecimal(123456), "EUR", "€"), fiatValue)
    val walletInfo = WalletInfo(
      wallet = "a24863cb-e586-472f-9e8a-622834c20c52",
      name = "Melissa wallet",
      walletBalance = WalletBalance(fiatValue, fiatValue, tokenBalance, tokenBalance, tokenBalance),
      blocked = false,
      backupDate = 987654L,
      verified = false,
      logging = true
    )
    ActiveWalletContentLandscape(
      walletInfo, VerificationStatusCompound(CODE_REQUESTED, VERIFIED, VerificationType.CREDIT_CARD)
    )
  }

  companion object {
    const val ADDRESS_KEY = "address_key"
    const val MANAGE_WALLET_REQUEST_KEY = "manage_wallet_request_key"
    const val VERIFY_PAYMENT_METHOD = "verify_payment_method"
  }

  private fun navController(): NavController {
    val navHostFragment =
      requireActivity().supportFragmentManager.findFragmentById(R.id.main_host_container)
          as NavHostFragment
    return navHostFragment.navController
  }
}
