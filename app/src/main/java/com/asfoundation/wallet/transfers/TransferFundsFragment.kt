package com.asfoundation.wallet.transfers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.appcoins.wallet.core.utils.android_common.AmountUtils.formatMoney
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_medium_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_pink
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_white
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asf.wallet.R
import com.asfoundation.wallet.manage_wallets.ManageWalletFragment
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransferFundsFragment : BasePageViewFragment() {
  private val viewModel: TransferFundsViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { TransferFundsView() } }
  }

  @Composable
  fun TransferFundsView() {
    Scaffold(
      topBar = {
        Surface { TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() }) }
      },
      containerColor = WalletColors.styleguide_blue,
    ) { padding ->
      Column(modifier = Modifier.padding(padding)) {
        ScreenTitle()
        NavigationTransfer()
        NavigationCurrencies()
        CurrentBalance()
      }
    }
  }

  @Composable
  fun ScreenTitle() {
    Text(
      text = stringResource(R.string.transfer_button),
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      color = WalletColors.styleguide_light_grey,
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
        ButtonWithText(
          label = stringResource(item.label),
          backgroundColor = if (selected) styleguide_pink else styleguide_blue_secondary,
          labelColor = if (selected) styleguide_white else styleguide_medium_grey,
          onClick = { viewModel.clickedTransferItem.value = item.destination.ordinal })
      }
    }
  }

  @Composable
  fun NavigationCurrencies() {
    Row(
      modifier = Modifier
        .background(shape = CircleShape, color = styleguide_blue_secondary)
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      viewModel.currencyNavigationItems().forEach { item ->
        val selected = viewModel.clickedCurrencyItem.value == item.destination.ordinal
        ButtonWithText(
          label = stringResource(item.label),
          backgroundColor = if (selected) styleguide_pink else styleguide_blue_secondary,
          labelColor = if (selected) styleguide_white else styleguide_medium_grey,
          onClick = { viewModel.clickedCurrencyItem.value = item.destination.ordinal },
          textStyle = MaterialTheme.typography.bodySmall
        )
      }
    }
  }

  @Composable
  fun CurrentBalance() {
    Text(
      text =
      stringResource(
        id = R.string.p2p_send_current_balance_message,
        234857.toString()//walletInfo.walletBalance.creditsOnlyFiat.amount
          .formatMoney()
          ?: "",
        "APPC"//walletInfo.walletBalance.creditsOnlyFiat.symbol
      ),
      style = MaterialTheme.typography.bodyLarge,
      color = WalletColors.styleguide_light_grey,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }

  private fun copyAddressToClipBoard(address: String) {
    val clipboard =
      requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(ManageWalletFragment.ADDRESS_KEY, address)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
  }
}
