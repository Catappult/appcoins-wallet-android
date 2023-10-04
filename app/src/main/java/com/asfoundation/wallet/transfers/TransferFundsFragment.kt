package com.asfoundation.wallet.transfers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.feature.walletInfo.data.balance.WalletBalance
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_medium_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_white
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.VectorIconButton
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.appcoins.wallet.ui.widgets.component.WalletTextField
import com.asf.wallet.R
import com.asfoundation.wallet.manage_wallets.ManageWalletFragment
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.Error
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.InvalidAmountError
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.InvalidWalletAddressError
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.Loading
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.NavigateToOpenAppcConfirmationView
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.NavigateToOpenEthConfirmationView
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.NavigateToWalletBlocked
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.NoNetworkError
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.NotEnoughFundsError
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.Success
import com.asfoundation.wallet.transfers.TransferFundsViewModel.UiState.UnknownError
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity
import com.asfoundation.wallet.ui.bottom_navigation.CurrencyDestinations
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

  private val viewModel: TransferFundsViewModel by viewModels()
  private var addressTextValue: MutableState<String> = mutableStateOf("")

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View {
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
        Surface { TopBar(onClickSupport = { viewModel.displayChat() }) }
      },
      containerColor = styleguide_blue,
    ) { padding ->
      Column(
        modifier = Modifier
          .padding(padding)
          .padding(horizontal = 16.dp)
          .verticalScroll(rememberScrollState())
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        ScreenTitle()
        NavigationTransfer()
        Spacer(Modifier.height(8.dp))
        CenterContent()
      }
    }
  }

  @Composable
  fun ScreenTitle() {
    Text(
      text = stringResource(R.string.transfer_button),
      modifier = Modifier.padding(8.dp),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = styleguide_light_grey,
    )
  }

  @Composable
  fun NavigationTransfer() {
    Row(
      modifier = Modifier
        .background(shape = CircleShape, color = styleguide_blue_secondary)
        .padding(horizontal = 4.dp)
    ) {
      viewModel.transferNavigationItems().forEach { item ->
        val selected = viewModel.clickedTransferItem.value == item.destination.ordinal
        ButtonWithText(label = stringResource(item.label),
          backgroundColor = if (selected) styleguide_pink else styleguide_blue_secondary,
          labelColor = if (selected) styleguide_white else styleguide_medium_grey,
          onClick = { viewModel.clickedTransferItem.value = item.destination.ordinal })
      }
    }
  }

  @Composable
  fun CenterContent() {
    when (val uiState = viewModel.uiState.collectAsState().value) {
      is Success -> {
        Column(
          modifier = Modifier
            .fillMaxHeight(),
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          when (viewModel.clickedTransferItem.value ?: SEND.ordinal) {
            SEND.ordinal -> {
              Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NavigationCurrencies()
                CurrentBalance(uiState.walletInfo.walletBalance)
                AddressTextField()
                AmountTextField()
              }
              SendButton()
            }

            RECEIVE.ordinal -> {
              QrCodeCard(uiState.walletInfo.wallet)
            }
          }
        }
      }

      is TransferFundsViewModel.UiState.SuccessAppcCreditsTransfer -> transferNavigator.openSuccessView(
        walletAddress = uiState.walletAddress,
        amount = uiState.amount,
        currency = uiState.currency,
        mainNavController = navController()
      )

      is NavigateToOpenAppcConfirmationView -> {
        transferNavigator.openAppcConfirmationView(
          walletAddress = uiState.walletAddress,
          toWalletAddress = uiState.toWalletAddress,
          amount = uiState.amount
        )
        navController().popBackStack()
      }

      is NavigateToOpenEthConfirmationView -> {
        transferNavigator.openEthConfirmationView(
          walletAddress = uiState.walletAddress,
          toWalletAddress = uiState.toWalletAddress,
          amount = uiState.amount
        )
        navController().popBackStack()
      }

      NavigateToWalletBlocked -> transferNavigator.showWalletBlocked()

      Loading -> Loading()

      Error -> {
        Toast.makeText(
          context,
          stringResource(R.string.unknown_error),
          LENGTH_SHORT
        ).show()
        viewModel.getWalletInfo()
      }

      InvalidAmountError -> {
        Toast.makeText(
          context,
          stringResource(R.string.error_invalid_amount),
          LENGTH_SHORT
        ).show()
        viewModel.getWalletInfo()
      }

      InvalidWalletAddressError -> {
        Toast.makeText(
          context,
          stringResource(R.string.error_invalid_address),
          LENGTH_SHORT
        ).show()
        viewModel.getWalletInfo()
      }

      NoNetworkError -> {
        Toast.makeText(
          context,
          stringResource(R.string.activity_iab_no_network_message),
          LENGTH_SHORT
        ).show()
        viewModel.getWalletInfo()
      }

      NotEnoughFundsError -> {
        Toast.makeText(
          context,
          stringResource(R.string.p2p_send_error_not_enough_funds),
          LENGTH_SHORT
        ).show()
        viewModel.getWalletInfo()
      }

      UnknownError -> {
        Toast.makeText(
          context,
          stringResource(R.string.unknown_error),
          LENGTH_SHORT
        ).show()
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
  fun NavigationCurrencies() {
    Row(
      modifier = Modifier
        .background(shape = CircleShape, color = styleguide_blue_secondary)
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      viewModel.currencyNavigationItems().forEach { item ->
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clickable { viewModel.clickedCurrencyItem.value = item.destination.ordinal },
          contentAlignment = Alignment.Center
        ) {
          val selected = viewModel.clickedCurrencyItem.value == item.destination.ordinal
          ButtonWithText(
            label = stringResource(item.label),
            backgroundColor = if (selected) styleguide_pink else styleguide_blue_secondary,
            labelColor = if (selected) styleguide_white else styleguide_medium_grey,
            onClick = { viewModel.clickedCurrencyItem.value = item.destination.ordinal },
            textStyle = MaterialTheme.typography.bodySmall,
            buttonType = ButtonType.LARGE
          )
        }
      }
    }
  }

  @Composable
  fun CurrentBalance(walletBalance: WalletBalance) {
    val balance = when (viewModel.clickedCurrencyItem.value) {
      CurrencyDestinations.APPC.ordinal -> walletBalance.appcBalance.token
      CurrencyDestinations.ETHEREUM.ordinal -> walletBalance.ethBalance.token
      CurrencyDestinations.APPC_C.ordinal -> walletBalance.creditsBalance.token
      else -> walletBalance.creditsBalance.token
    }
    Text(
      modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
      text = stringResource(
        id = R.string.p2p_send_current_balance_message,
        balance.amount.toString().formatMoney() ?: "",
        balance.symbol
      ),
      style = MaterialTheme.typography.bodyLarge,
      color = styleguide_light_grey,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )

  }

  @Preview
  @Composable
  fun AddressTextField() {
    var address by rememberSaveable { addressTextValue }
    Text(
      modifier = Modifier.padding(horizontal = 8.dp),
      text = stringResource(R.string.p2p_send_body),
      style = MaterialTheme.typography.bodySmall,
      color = styleguide_light_grey
    )
    Row {
      WalletTextField(
        value = address,
        placeHolder = stringResource(R.string.hint_recipient_address),
        backgroundColor = styleguide_blue_secondary,
        keyboardType = KeyboardType.Ascii,
        trailingIcon = {
          VectorIconButton(
            painter = painterResource(R.drawable.ic_qrcode),
            contentDescription = R.string.scan_qr,
            onClick = { transferNavigator.showQrCodeScreen() },
            paddingIcon = 4.dp,
            background = styleguide_blue_secondary
          )
        }) { newAddress ->
        address = newAddress
        viewModel.currentAddedAddress = newAddress
      }
    }
  }

  @Preview
  @Composable
  fun AmountTextField() {
    var amount by rememberSaveable { mutableStateOf("") }
    WalletTextField(
      amount,
      stringResource(R.string.hint_amount),
      backgroundColor = styleguide_blue_secondary,
      keyboardType = KeyboardType.Decimal
    ) { newAmount ->
      amount = newAmount
      viewModel.currentAddedAmount = newAmount
    }
  }

  @Composable
  fun QrCodeCard(address: String) {
    Card(colors = CardDefaults.cardColors(containerColor = styleguide_blue_secondary)) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "My wallet address",
          style = MaterialTheme.typography.bodyLarge,
          color = styleguide_light_grey,
          fontWeight = FontWeight.Medium
        )
        Text(
          modifier = Modifier.padding(bottom = 8.dp),
          text = address,
          style = MaterialTheme.typography.bodySmall,
          color = styleguide_dark_grey,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Image(
          modifier = Modifier
            .background(
              color = styleguide_white, shape = RoundedCornerShape(16.dp)
            )
            .size(200.dp)
            .padding(8.dp),
          bitmap = createQRImage(address)!!.asImageBitmap(),
          contentDescription = stringResource(R.string.scan_qr)
        )
        Spacer(Modifier.height(8.dp))
        ButtonWithText(
          label = stringResource(R.string.wallet_view_copy_button),
          onClick = { copyAddressToClipBoard(address) },
          labelColor = styleguide_light_grey,
          outlineColor = styleguide_light_grey,
          buttonType = ButtonType.LARGE
        )
      }
    }
  }

  @Composable
  fun SendButton() {
    Column(Modifier.padding(vertical = 32.dp)) {
      ButtonWithText(
        label = stringResource(R.string.transfer_send_button),
        onClick = {
          if (
            viewModel.currentAddedAmount.isNotEmpty() &&
            viewModel.currentAddedAddress.isNotEmpty()
          ) {
            val currency = when (viewModel.clickedCurrencyItem.value) {
              CurrencyDestinations.APPC.ordinal -> TransferFundsViewModel.Currency.APPC
              CurrencyDestinations.ETHEREUM.ordinal -> TransferFundsViewModel.Currency.ETH
              CurrencyDestinations.APPC_C.ordinal -> TransferFundsViewModel.Currency.APPC_C
              else -> TransferFundsViewModel.Currency.APPC_C
            }
            viewModel.onClickSend(
              TransferFundsViewModel.TransferData(
                walletAddress = viewModel.currentAddedAddress,
                currency = currency,
                amount = viewModel.currentAddedAmount.toBigDecimal(),
              ),
              requireContext().packageName
            )
          }
        },
        backgroundColor = styleguide_pink,
        labelColor = styleguide_light_grey,
        buttonType = ButtonType.LARGE
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
    } else if (resultCode == CommonStatusCodes.SUCCESS && requestCode == TransferFragmentNavigator.BARCODE_READER_REQUEST_CODE) {
      data?.let { data ->
        val barcode = data.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
        QRUri.parse(barcode?.displayValue).let {
          if (it.address != BarcodeCaptureActivity.ERROR_CODE) {
            addressTextValue.value = it.address
            viewModel.currentAddedAddress = it.address
          } else {
            Toast.makeText(context, R.string.toast_qr_code_no_address, LENGTH_SHORT)
              .show()
          }
        }
      }
    }
  }

  private fun navController(): NavController {
    val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(
      R.id.main_host_container
    ) as NavHostFragment
    return navHostFragment.navController
  }

  companion object {
    private const val QR_CODE_SIZE = 400
    const val TRANSFER_KEY = "entry_screen_selected"
  }
}
