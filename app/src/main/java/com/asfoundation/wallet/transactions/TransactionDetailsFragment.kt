package com.asfoundation.wallet.transactions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils.getDayAndHour
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TransactionDetailHeader
import com.appcoins.wallet.ui.widgets.TransactionDetailItem
import com.appcoins.wallet.ui.widgets.component.ButtonType
import com.appcoins.wallet.ui.widgets.component.ButtonWithText
import com.asf.wallet.R
import com.asfoundation.wallet.transactions.TransactionDetailsViewModel.UiState
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionDetailsFragment : BasePageViewFragment() {
  private val viewModel: TransactionDetailsViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent { TransactionDetailView(viewModel.uiState.collectAsState().value) }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireArguments().run {
      viewModel.updateTransaction(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
          getParcelable(TRANSACTION_KEY, TransactionModel::class.java)
        else getParcelable(TRANSACTION_KEY)
      )
    }
  }

  @Composable
  fun TransactionDetailView(uiState: UiState) {
    Scaffold(
      topBar = {
        Surface {
          TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() })
        }
      },
      containerColor = WalletColors.styleguide_blue
    ) { padding ->
      when (uiState) {
        is UiState.Success -> {
          TransactionsDetail(
            paddingValues = padding,
            with(uiState.transaction) { cardInfoByType().copy(date = date.getDayAndHour()) })
        }

        is UiState.Loading -> {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            CircularProgressIndicator()
          }
        }
      }
    }
  }

  @Composable
  fun TransactionsDetail(paddingValues: PaddingValues, transactionCardInfo: TransactionCardInfo) {
    with(transactionCardInfo) {
      LazyColumn(modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()) {
        item {
          Text(
            text = stringResource(R.string.transaction_details_header),
            modifier = Modifier.padding(start = 24.dp, top = 8.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = styleguide_light_grey,
          )
        }
        item {
          if (app != null)
            Text(
              text = app,
              modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
              style = MaterialTheme.typography.bodySmall,
              color = WalletColors.styleguide_dark_grey
            )
        }
        item {
          Card(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
              TransactionDetailHeader(
                icon = icon,
                appIcon = appIcon,
                amount = amount,
                convertedAmount = amountSubtitle,
                subIcon = subIcon,
                type = stringResource(title),
                textDecoration = textDecoration,
                description = description
              )

              TransactionDetailItem(
                stringResource(R.string.transaction_status_label),
                stringResource(status.description),
                status.color
              )

              TransactionDetailItem(
                stringResource(R.string.transaction_category_label),
                stringResource(title)
              )

              TransactionDetailItem(stringResource(R.string.transaction_date_label), date)

              if (id != null)
                TransactionDetailItem(
                  label = stringResource(R.string.transaction_order_reference_label),
                  data = id,
                  allowCopy = true,
                  onClick = { copyOrderIdToClipBoard(id) })

              if (from != null)
                TransactionDetailItem(stringResource(R.string.label_from), from)

              if (to != null)
                TransactionDetailItem(stringResource(R.string.transaction_to_label), to)

              Spacer(modifier = Modifier.padding(vertical = 16.dp))

              ButtonWithText(
                label = stringResource(R.string.transaction_more_details_label),
                onClick = { openTransactionUrl(transactionUrl) },
                labelColor = styleguide_light_grey,
                outlineColor = styleguide_light_grey,
                buttonType = ButtonType.LARGE
              )
            }
          }
        }

        item {
          if (failedMessage != null)
            Column(
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = stringResource(failedMessage),
                style = MaterialTheme.typography.bodySmall,
                color = WalletColors.styleguide_dark_grey,
                textAlign = TextAlign.Center
              )

              IconButton(
                onClick = { viewModel.displayChat() }, modifier = Modifier.width(160.dp)
              ) {
                Icon(
                  painter = painterResource(R.drawable.ic_support_chat),
                  contentDescription = stringResource(R.string.title_support),
                  tint = Color.Unspecified,
                )
              }
            }
        }
      }
    }
  }

  private fun copyOrderIdToClipBoard(orderReference: String) {
    val clipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(ORDER_KEY, orderReference)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
  }

  private fun openTransactionUrl(transactionUrl: String) =
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(transactionUrl)))

  @Preview
  @Composable
  fun PreviewTransactionsDetail() {
    TransactionsDetail(
      PaddingValues(8.dp),
      TransactionCardInfo(
        icon = R.drawable.ic_transaction_reward,
        title = R.string.transaction_type_rejected_eskills_ticket,
        amount = "-€12.73",
        app = "Horizon",
        description = "Rejected Purchase",
        amountSubtitle = "-30.45 APPC-C",
        subIcon = R.drawable.ic_transaction_rejected_mini,
        textDecoration = TextDecoration.LineThrough,
        date = "Aug, 30 2022, 12:30AM",
        status = StatusType.PENDING,
        id = "0x385e12aa45036de011b8e67ceef307791c64a93bb01089d85b0fc2eda6a5aaec",
        from = "0x31a16aDF2D5FC73F149fBB779D20c036678b1bBD",
        to = "0xd21e10A8bd5917Fa57776dE4654284dCc8434F23",
        transactionUrl =
        "https://appcexplorer.io/transaction/0x142e7c14059728205966dd1389feb905cd732fe7f6fbd23f1fceaf2d50f14242"
      )
    )
  }

  companion object {
    const val ORDER_KEY = "order_key"
    const val TRANSACTION_KEY = "transaction_key"
  }
}
