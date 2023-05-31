package com.asfoundation.wallet.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.paging.LoadState.Loading
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.appcoins.wallet.core.utils.android_common.DateFormatterUtils.getDay
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TransactionCard
import com.appcoins.wallet.ui.widgets.TransactionSeparator
import com.asf.wallet.R
import com.asfoundation.wallet.transactions.Transaction.TransactionType.*
import com.asfoundation.wallet.transactions.TransactionsListViewModel.*
import com.asfoundation.wallet.transactions.TransactionsListViewModel.UiModel.TransactionItem
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransactionsListFragment : BasePageViewFragment() {
  @Inject
  lateinit var transactionsNavigator: TransactionsNavigator

  private val viewModel: TransactionsListViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return ComposeView(requireContext()).apply {
      setContent {
        TransactionsView(viewModel.uiState.collectAsState().value)
      }
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  fun TransactionsView(uiState: UiState) {
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
          TransactionsList(
            paddingValues = padding,
            walletInfo = uiState.walletInfo
          )
        }

        UiState.Loading -> {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            CircularProgressIndicator()
          }
        }

        else -> {}
      }
    }
  }

  @Composable
  fun TransactionsList(
    paddingValues: PaddingValues,
    walletInfo: WalletInfoModel
  ) {
    val items = viewModel.fetchTransactions(walletInfo).collectAsLazyPagingItems()

    LazyColumn(
      modifier = Modifier
        .padding(
          top = paddingValues.calculateTopPadding(),
          bottom = paddingValues.calculateBottomPadding(),
          start = 16.dp,
          end = 16.dp
        )
        .fillMaxSize()
    ) {
      item {
        Text(
          text = stringResource(R.string.intro_transactions_header),
          modifier = Modifier.padding(8.dp),
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = WalletColors.styleguide_light_grey,
        )
      }

      items(
        count = items.itemCount,
        key = items.itemKey(),
        contentType = items.itemContentType { it }
      ) { index ->
        when (val uiModel = items[index]) {
          is TransactionItem -> {
            with(uiModel.transaction.cardInfoByType()) {
              TransactionCard(
                icon = icon,
                appIcon = appIcon,
                title = stringResource(id = title),
                description = description ?: app,
                amount = amount,
                convertedAmount = amountSubtitle,
                subIcon = subIcon,
                onClick = { navigateToTransactionDetails(uiModel.transaction) },
                textDecoration = textDecoration
              )
            }
            Spacer(modifier = Modifier.padding(top = 4.dp))
          }


          is UiModel.SeparatorItem -> TransactionSeparator(uiModel.date.getDay())

          null -> {}
        }
      }

      when (items.loadState.refresh) {
        is Loading -> {
          item {
            Row(
              modifier = Modifier.fillParentMaxSize(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              CircularProgressIndicator()
            }
          }
        }

        else -> {}
      }

      when (items.loadState.append) {
        is Loading -> {
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              CircularProgressIndicator()
            }
          }
        }

        else -> {}
      }
    }
  }

  private fun navigateToTransactionDetails(transaction: TransactionModel) =
    transactionsNavigator.navigateToTransactionDetails(navController(), transaction)

  private fun navController(): NavController {
    val navHostFragment =
      requireActivity().supportFragmentManager.findFragmentById(R.id.main_host_container)
          as NavHostFragment
    return navHostFragment.navController
  }
}
