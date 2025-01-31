package com.asfoundation.wallet.transfers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.core.analytics.analytics.common.ButtonsAnalytics
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.core.utils.android_common.extensions.StringUtils.masked
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_grey_new
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_medium_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_white
import com.appcoins.wallet.ui.widgets.VectorIconButton
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletTextField
import com.appcoins.wallet.ui.widgets.top_bar.TopBar
import com.asf.wallet.R
import com.asfoundation.wallet.manage_wallets.ManageWalletFragment
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.Error
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.InvalidAmountError
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.InvalidWalletAddressError
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.Loading
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.NavigateToWalletBlocked
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.NoNetworkError
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.NotEnoughFundsError
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.Success
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.UnknownError
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity
import com.asfoundation.wallet.ui.bottom_navigation.TransferDestinations.RECEIVE
import com.asfoundation.wallet.ui.bottom_navigation.TransferDestinations.SEND
import com.asfoundation.wallet.ui.transact.TransferFragmentNavigator
import com.asfoundation.wallet.util.QRUri
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransferFundsFragment : BasePageViewFragment() {
  @Inject
  lateinit var transferNavigator: TransferFragmentNavigator

  @Inject
  lateinit var buttonsAnalytics: ButtonsAnalytics
  private val fragmentName = this::class.java.simpleName

  private val viewModel: TransferFundsViewModel by viewModels()
  private var addressTextValue: MutableState<String> = mutableStateOf("")

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    return ComposeView(requireContext()).apply { setContent { TransferFundsView() } }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (viewModel.clickedTransferItem.value == null)
      viewModel.clickedTransferItem.value = requireArguments().getInt(TRANSFER_KEY, SEND.ordinal)
  }

  @Composable
  fun TransferFundsView() {
    Scaffold(
      topBar = {
        Surface {
          TopBar(
            onClickSupport = { viewModel.displayChat() },
            fragmentName = fragmentName,
            buttonsAnalytics = buttonsAnalytics
          )
        }
      },
      containerColor = styleguide_blue,
      bottomBar = {
        if (
          viewModel.uiState.collectAsState().value is Success &&
          (viewModel.clickedTransferItem.value ?: SEND.ordinal) == SEND.ordinal
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp, end = 16.dp),
          ) {
            SendButton()
          }
        }
      }
    ) { padding ->
      Column(
        modifier =
        Modifier
          .padding(padding)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
//        ScreenTitle(stringResource(R.string.transfer_button))
        Card(
          colors = CardDefaults.cardColors(styleguide_blue_secondary),
          modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
            .clip(shape = RoundedCornerShape(24.dp))
        ) {
          Column(
            modifier =
            Modifier
              .padding(8.dp)
              .verticalScroll(rememberScrollState())
              .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            NavigationTransfer()
            Spacer(Modifier.height(8.dp))
            CenterContent()
          }
        }
      }
    }
  }

  @Composable
  fun NavigationTransfer() {
    Row(
      modifier =
      Modifier
        .fillMaxWidth()
        .background(shape = CircleShape, color = styleguide_blue)
        .padding(4.dp),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      viewModel.transferNavigationItems().forEach { item ->
        val selected = viewModel.clickedTransferItem.value == item.destination.ordinal
        ButtonWithText(
          label = stringResource(item.label),
          backgroundColor = if (selected) styleguide_grey_new else styleguide_blue,
          labelColor = if (selected) styleguide_white else styleguide_medium_grey,
          onClick = { viewModel.clickedTransferItem.value = item.destination.ordinal },
          fragmentName = fragmentName,
          buttonsAnalytics = buttonsAnalytics,
          modifier =
          Modifier
            .weight(1f)
            .height(40.dp)
        )
      }
    }
  }

  @Composable
  fun CenterContent() {
    when (val uiState = viewModel.uiState.collectAsState().value) {
      is Success -> {
        Column(
          modifier = Modifier.fillMaxHeight(),
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          when (viewModel.clickedTransferItem.value ?: SEND.ordinal) {
            SEND.ordinal -> {
              Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                CurrentBalance(uiState.walletInfo.walletBalance)
                Separator()
                AddressTextField()
                AmountTextField(uiState.walletInfo.walletBalance)
              }
            }

            RECEIVE.ordinal -> {
              QrCodeCard(uiState.walletInfo.wallet)
            }
          }
        }
      }

      is TransferFundsViewModel.UiState.SuccessAppcCreditsTransfer ->
        transferNavigator.openSuccessView(
          walletAddress = uiState.walletAddress,
          amount = uiState.amount,
          currency = uiState.currency,
          mainNavController = navController()
        )

      NavigateToWalletBlocked -> transferNavigator.showWalletBlocked()
      Loading -> Loading()
      InvalidAmountError -> {
        Toast.makeText(context, stringResource(R.string.error_invalid_amount), LENGTH_SHORT).show()
        viewModel.getWalletInfo()
      }

      InvalidWalletAddressError -> {
        Toast.makeText(context, stringResource(R.string.error_invalid_address), LENGTH_SHORT).show()
        viewModel.getWalletInfo()
      }

      NoNetworkError -> {
        Toast.makeText(
          context, stringResource(R.string.activity_iab_no_network_message), LENGTH_SHORT
        )
          .show()
        viewModel.getWalletInfo()
      }

      NotEnoughFundsError -> {
        Toast.makeText(
          context, stringResource(R.string.p2p_send_error_not_enough_funds), LENGTH_SHORT
        )
          .show()
        viewModel.getWalletInfo()
      }

      Error,
      UnknownError -> {
        Toast.makeText(context, stringResource(R.string.unknown_error), LENGTH_SHORT).show()
        viewModel.getWalletInfo()
      }

      else -> {}
    }
  }

  @Composable
  fun Loading() {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      CircularProgressIndicator()
    }
  }

  @Composable
  fun CurrentBalance(walletBalance: WalletBalance) {
    val balance = walletBalance.creditsOnlyFiat
    Column(
      modifier = Modifier.padding(bottom = 24.dp),
    ) {
      Text(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 4.dp),
        text = stringResource(R.string.aptoide_balance),
        style = MaterialTheme.typography.bodySmall,
        color = styleguide_dark_grey,
        fontSize = 10.sp
      )
      Text(
        modifier = Modifier.padding(horizontal = 8.dp),
        text = "${balance.symbol}${
          balance.amount.toString().formatMoney() ?: ""
        } ${balance.currency}",
        style = MaterialTheme.typography.bodyLarge,
        color = styleguide_light_grey,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 26.sp,
      )
    }
  }

  @Composable
  fun Separator() {
    Spacer(
      modifier =
      Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(styleguide_grey_new)
    )
  }

  @Preview
  @Composable
  fun TransferFundsScreen() {
    TransferFundsView()
  }

  @Preview
  @Composable
  fun AddressTextField() {
    var address by rememberSaveable { addressTextValue }
    Text(
      modifier = Modifier
        .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
      text = stringResource(R.string.transfer_send_to_title),
      style = MaterialTheme.typography.bodySmall,
      fontSize = 14.sp,
      color = styleguide_light_grey
    )
    Row {
      WalletTextField(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
        value = address,
        placeHolder = stringResource(R.string.hint_recipient_address),
        backgroundColor = styleguide_blue,
        keyboardType = KeyboardType.Ascii,
        roundedCornerShape = RoundedCornerShape(16.dp),
        trailingIcon = {
          VectorIconButton(
            painter = painterResource(R.drawable.qr_code2),
            contentDescription = R.string.scan_qr,
            onClick = { transferNavigator.showQrCodeScreen() },
            paddingIcon = 6.dp,
            fragmentName = fragmentName,
            buttonsAnalytics = buttonsAnalytics
          )
        }
      ) { newAddress ->
        address = newAddress
        viewModel.currentAddedAddress = newAddress
      }
    }
  }

  @Composable
  fun AmountTextField(walletBalance: WalletBalance) {
    val balance = walletBalance.creditsOnlyFiat
    var amount by rememberSaveable { mutableStateOf("") }
    Text(
      modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
      text = stringResource(R.string.transfer_send_amount_title),
      style = MaterialTheme.typography.bodySmall,
      fontSize = 14.sp,
      color = styleguide_light_grey
    )
    WalletTextField(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 16.dp),
      value = amount,
      placeHolder = "${balance.symbol}0.00 ${balance.currency}",
      backgroundColor = styleguide_blue,
      keyboardType = KeyboardType.Decimal,
      roundedCornerShape = RoundedCornerShape(16.dp),
      trailingIcon = {
        Card(
          colors = CardDefaults.cardColors(styleguide_blue_secondary),
          modifier = Modifier
            .defaultMinSize(minWidth = 28.dp, minHeight = 20.dp)
            .padding(top = 2.dp, bottom = 2.dp, start = 2.dp, end = 2.dp)
            .clip(shape = RoundedCornerShape(3.dp))
            .clickable {
              amount = balance.amount.toString()
              viewModel.currentAddedAmount = balance.amount.toString()
            }
        ) {
          Row {
            Text(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              text = stringResource(R.string.max),
              style = MaterialTheme.typography.bodySmall,
              fontSize = 10.sp,
              color = styleguide_light_grey
            )
          }

        }
      },
    ) { newAmount ->
      amount = newAmount
      viewModel.currentAddedAmount = newAmount
    }
  }

  @Composable
  fun QrCodeCard(address: String) {
    Card(colors = CardDefaults.cardColors(containerColor = styleguide_blue_secondary)) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Image(
          modifier =
          Modifier
            .background(color = styleguide_white, shape = RoundedCornerShape(16.dp))
            .size(200.dp)
            .padding(8.dp),
          bitmap = createQRImage(address)!!.asImageBitmap(),
          contentDescription = stringResource(R.string.scan_qr)
        )
        Spacer(Modifier.height(8.dp))
        Card(
          colors = CardDefaults.cardColors(styleguide_blue),
          modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = 0.dp, start = 0.dp, end = 0.dp)
            .clip(shape = RoundedCornerShape(16.dp))
        ) {
          Column {
            Text(
              modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
              text = stringResource(R.string.transfer_public_wallet_address_title),
              style = MaterialTheme.typography.bodySmall,
              fontSize = 10.sp,
              color = styleguide_dark_grey
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
              verticalAlignment = Alignment.Top
            ) {
              Text(
                modifier = Modifier
                  .weight(1f),
                text = address.masked(
                  nStartChars = 7,
                  nEndChars = 7
                ),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                color = styleguide_light_grey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )

              IconButton(
                onClick = { shareAddress(address) },
                modifier = Modifier.size(32.dp),
              ) {
                Icon(
                  painter = painterResource(R.drawable.ic_export),
                  contentDescription = stringResource(R.string.wallet_view_share_button),
                  tint = styleguide_white,
                  modifier = Modifier.size(16.dp)
                )
              }
              IconButton(
                onClick = { copyAddressToClipBoard(address) },
                modifier = Modifier.size(32.dp),
              ) {
                Icon(
                  painter = painterResource(R.drawable.ic_copy_2),
                  contentDescription = stringResource(R.string.copy),
                  tint = styleguide_white,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  @Composable
  fun SendButton() {
    Column(Modifier.padding(vertical = 32.dp)) {
      ButtonWithText(
        label = stringResource(R.string.transfer_send_button),
        onClick = {
          if (viewModel.currentAddedAmount.isNotEmpty() &&
            viewModel.currentAddedAddress.isNotEmpty()
          ) {
            try {
              val userCurrency =
                (viewModel.uiState.value as Success).walletInfo.walletBalance.creditsOnlyFiat.currency
              viewModel.onClickSend(
                TransferFundsViewModel.TransferData(
                  walletAddress = viewModel.currentAddedAddress,
                  currency = userCurrency,
                  amount = viewModel.currentAddedAmount.toBigDecimal(),
                ),
                requireContext().packageName
              )
            } catch (e: Exception) {
              Log.d(
                TransferFundsFragment::class.java.simpleName,
                "Send transfer error: ${e.message}"
              )
            }
          }
        },
        backgroundColor = styleguide_pink,
        labelColor = styleguide_light_grey,
        buttonType = ButtonType.LARGE,
        fragmentName = fragmentName,
        buttonsAnalytics = buttonsAnalytics
      )
    }
  }

  private fun copyAddressToClipBoard(address: String) {
    val clipboard =
      requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(ManageWalletFragment.ADDRESS_KEY, address)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, R.string.copied_to_clipboard, LENGTH_SHORT).show()
  }

  private fun shareAddress(walletAddress: String) =
    ShareCompat.IntentBuilder(requireActivity())
      .setText(walletAddress)
      .setType("text/plain")
      .setChooserTitle(resources.getString(R.string.share_via))
      .startChooser()

  private fun createQRImage(address: String): Bitmap? {
    return try {
      val bitMatrix =
        QRCodeWriter().encode(address, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, null)
      val barcodeEncoder = BarcodeEncoder()
      barcodeEncoder.createBitmap(bitMatrix)
    } catch (e: Exception) {
      Toast.makeText(context, getString(R.string.error_fail_generate_qr), LENGTH_SHORT).show()
      null
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == TransferFragmentNavigator.TRANSACTION_CONFIRMATION_REQUEST_CODE) {
      transferNavigator.navigateBack()
    } else if (resultCode == CommonStatusCodes.SUCCESS &&
      requestCode == TransferFragmentNavigator.BARCODE_READER_REQUEST_CODE
    ) {
      data?.let { data ->
        val barcode = data.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
        QRUri.parse(barcode?.displayValue).let {
          if (it.address != BarcodeCaptureActivity.ERROR_CODE) {
            addressTextValue.value = it.address
            viewModel.currentAddedAddress = it.address
          } else {
            Toast.makeText(context, R.string.toast_qr_code_no_address, LENGTH_SHORT).show()
          }
        }
      }
    }
  }

  private fun navController(): NavController {
    val navHostFragment =
      requireActivity().supportFragmentManager.findFragmentById(R.id.main_host_container)
          as NavHostFragment
    return navHostFragment.navController
  }

  companion object {
    private const val QR_CODE_SIZE = 400
    const val TRANSFER_KEY = "entry_screen_selected"
  }
}
