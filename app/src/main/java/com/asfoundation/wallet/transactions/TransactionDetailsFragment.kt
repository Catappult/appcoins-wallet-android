package com.asfoundation.wallet.transactions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TransactionDetailHeader
import com.appcoins.wallet.ui.widgets.TransactionDetailItem
import com.appcoins.wallet.ui.widgets.component.LargeButtonWithText
import com.asf.wallet.R
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.transactions.Transaction.TransactionCardInfo
import com.asfoundation.wallet.transactions.Transaction.TransactionType.*
import com.asfoundation.wallet.transactions.TransactionsListViewModel.*
import com.asfoundation.wallet.transactions.TransactionsListViewModel.UiState.*
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransactionDetailsFragment : BasePageViewFragment() {

  @Inject
  lateinit var displayChat: DisplayChatUseCase

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply { setContent { TransactionDetailView() } }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun TransactionDetailView() {
    Scaffold(
      topBar = {
        Surface(shadowElevation = 4.dp) {
          TopBar(
            isMainBar = false,
            onClickSupport = { displayChat() })
        }
      },
      containerColor = WalletColors.styleguide_blue
    ) { padding ->
      TransactionsDetail(
        paddingValues = padding,
        TransactionCardInfo(
          icon = R.drawable.ic_transaction_reward,
          title = R.string.transaction_type_rejected_eskills_ticket,
          amount = "-€12.73",
          description = "Rejected Purchase",
          convertedAmount = "-30.45 APPC-C",
          subIcon = R.drawable.ic_transaction_rejected_mini,
          textDecoration = TextDecoration.LineThrough,
          date = "Aug, 30 2022, 12:30AM",
          status = StatusType.SUCCESS,
          id = "0x385e12aa45036de011b8e67ceef307791c64a93bb01089d85b0fc2eda6a5aaec",
          from = "0x31a16aDF2D5FC73F149fBB779D20c036678b1bBD",
          to = "0xd21e10A8bd5917Fa57776dE4654284dCc8434F23",
          transactionUrl = "https://appcexplorer.io/transaction/0x142e7c14059728205966dd1389feb905cd732fe7f6fbd23f1fceaf2d50f14242"
        )
      )
    }
  }

  @Composable
  fun TransactionsDetail(paddingValues: PaddingValues, transactionCardInfo: TransactionCardInfo) {
    with(transactionCardInfo) {
      LazyColumn(
        modifier = Modifier
          .padding(paddingValues)
          .fillMaxSize()
      ) {
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
          if (description != null)
            Text(
              text = description,
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
                convertedAmount = convertedAmount,
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

              if (id != null) TransactionDetailItem(
                label = stringResource(R.string.transaction_order_reference_label),
                data = id,
                allowCopy = true,
                onClick = { copyOrderIdToClipBoard(id) }
              )

              if (from != null) TransactionDetailItem(stringResource(R.string.label_from), from)

              if (to != null) TransactionDetailItem(
                stringResource(R.string.transaction_to_label),
                to
              )

              Spacer(modifier = Modifier.padding(vertical = 16.dp))

              LargeButtonWithText(
                label = R.string.transaction_more_details_label,
                onClick = { openTransactionUrl(transactionUrl) },
                labelColor = styleguide_light_grey,
                outlineColor = styleguide_light_grey
              )
            }
          }
        }

        item {
          Column(
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "This purchase has been rejected.\nFor more information contact support.",
              style = MaterialTheme.typography.bodySmall,
              color = WalletColors.styleguide_dark_grey,
              textAlign = TextAlign.Center
            )

            IconButton(onClick = { displayChat() }, modifier = Modifier.width(160.dp)) {
              Icon(
                painter = painterResource(R.drawable.ic_support),
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
    val clip = ClipData.newPlainText(KEY_ORDER, orderReference)
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
        description = "Rejected Purchase",
        convertedAmount = "-30.45 APPC-C",
        subIcon = R.drawable.ic_transaction_rejected_mini,
        textDecoration = TextDecoration.LineThrough,
        date = "Aug, 30 2022, 12:30AM",
        status = StatusType.PENDING,
        id = "0x385e12aa45036de011b8e67ceef307791c64a93bb01089d85b0fc2eda6a5aaec",
        from = "0x31a16aDF2D5FC73F149fBB779D20c036678b1bBD",
        to = "0xd21e10A8bd5917Fa57776dE4654284dCc8434F23",
        transactionUrl = "https://appcexplorer.io/transaction/0x142e7c14059728205966dd1389feb905cd732fe7f6fbd23f1fceaf2d50f14242"
      )
    )
  }

  companion object {
    const val KEY_ORDER = "key_order"
  }
}
