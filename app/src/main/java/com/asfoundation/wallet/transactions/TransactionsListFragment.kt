package com.asfoundation.wallet.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TransactionCard
import com.asf.wallet.R
import com.asfoundation.wallet.transactions.Transaction.TransactionStatus.SUCCESS
import com.asfoundation.wallet.transactions.Transaction.TransactionType.*
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.wallet.home.cardInfoByType

class TransactionsListFragment : BasePageViewFragment() {
  private val viewModel: TransactionsListViewModel by viewModels()

  val transactions = mapOf(
    "Apr, 24  2023" to listOf(
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = TOP_UP,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = "12.00",
        paidCurrency = "EUR",
        orderReference = null
      ),
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = BONUS,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = null,
        paidCurrency = null,
        orderReference = null
      )
    ),
    "Apr, 23  2023" to listOf(
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = TOP_UP,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = "12.00",
        paidCurrency = "EUR",
        orderReference = null
      ),
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = BONUS,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = null,
        paidCurrency = null,
        orderReference = null
      )
    ),
    "Apr, 22  2023" to listOf(
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = TOP_UP,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = "12.00",
        paidCurrency = "EUR",
        orderReference = null
      ),
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = BONUS,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = null,
        paidCurrency = null,
        orderReference = null
      )
    ),
    "Apr, 21  2023" to listOf(
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = TOP_UP,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = "12.00",
        paidCurrency = "EUR",
        orderReference = null
      ),
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = BONUS,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = null,
        paidCurrency = null,
        orderReference = null
      )
    ),
    "Apr, 20  2023" to listOf(
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = TOP_UP,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = "12.00",
        paidCurrency = "EUR",
        orderReference = null
      ),
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = BONUS,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = null,
        paidCurrency = null,
        orderReference = null
      )
    ),
    "Apr, 19  2023" to listOf(
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = TOP_UP,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = "12.00",
        paidCurrency = "EUR",
        orderReference = null
      ),
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = BONUS,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = null,
        paidCurrency = null,
        orderReference = null
      )
    ),
    "Apr, 18  2023" to listOf(
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = TOP_UP,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = "12.00",
        paidCurrency = "EUR",
        orderReference = null
      ),
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = BONUS,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = null,
        paidCurrency = null,
        orderReference = null
      )
    ),
    "Apr, 17  2023" to listOf(
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = TOP_UP,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = "12.00",
        paidCurrency = "EUR",
        orderReference = null
      ),
      Transaction(
        transactionId = "0xceb9d0d0dbf0c4bbe1f60b4252868218435b5e2dbaeb807ff556d7c2b707cb12",
        type = BONUS,
        subType = null,
        method = Transaction.Method.APPC_C,
        title = null,
        description = null,
        perk = null,
        approveTransactionId = null,
        timeStamp = 1682328974384,
        processedTime = 1682328974747,
        status = SUCCESS,
        value = "135787313022899994624",
        from = "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
        to = "0xdf584cfc73008ab8253e4bbb1c30e65bbc026f9f",
        details = null,
        currency = "APPC",
        operations = listOf(),
        linkedTx = listOf(),
        paidAmount = null,
        paidCurrency = null,
        orderReference = null
      )
    ),
  )

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        Scaffold(
          topBar = {
            Surface(shadowElevation = 4.dp) {
              TopBar(isMainBar = false)
            }
          },
          containerColor = WalletColors.styleguide_blue
        ) { padding ->
          TransactionsList(paddingValues = padding, transactionsGrouped = transactions)
        }
      }
    }
  }

  @Composable
  fun TransactionsList(
    transactionsGrouped: Map<String, List<Transaction>>,
    paddingValues: PaddingValues
  ) {
    LazyColumn(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
    )
    {
      item {
        Text(
          text = stringResource(R.string.intro_transactions_header),
          modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 8.dp),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_light_grey,
        )
      }

      transactionsGrouped.forEach { (date, transactionsForDate) ->
        item {
          Text(
            text = date,
            color = WalletColors.styleguide_medium_grey,
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp, top = 16.dp),
            style = MaterialTheme.typography.bodySmall
          )
        }

        items(transactionsForDate) { transaction ->
          Card(
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = WalletColors.styleguide_blue_secondary)
          ) {
            with(transaction.cardInfoByType()) {
              TransactionCard(
                icon = painterResource(id = icon),
                title = stringResource(id = title),
                description = description,
                amount = amount,
                currency = currency,
                subIcon = subIcon,
                onClick = { },
              )
            }
          }
        }
      }
    }
  }
}

