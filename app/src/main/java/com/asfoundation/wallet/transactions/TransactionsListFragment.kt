package com.asfoundation.wallet.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.TransactionCard
import com.asf.wallet.R
import com.asfoundation.wallet.transactions.Transaction.TransactionType.*
import com.asfoundation.wallet.transactions.TransactionsListViewModel.*
import com.asfoundation.wallet.transactions.TransactionsListViewModel.UiState.*
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.asfoundation.wallet.wallet.home.cardInfoByType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionsListFragment : BasePageViewFragment() {
  private val viewModel: TransactionsListViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
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
        Surface(shadowElevation = 4.dp) {
          TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() })
        }
      },
      containerColor = WalletColors.styleguide_blue
    ) { padding ->
      when (uiState) {
        is Success -> TransactionsList(
          paddingValues = padding,
          transactionsGrouped = uiState.transactions
        )

        Loading -> {
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
    transactionsGrouped: Map<String, List<TransactionModel>>,
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
        val maxHeight = (transactionsForDate.size * 80).dp

        item {
          Text(
            text = date,
            color = WalletColors.styleguide_medium_grey,
            modifier = Modifier.padding(start = 24.dp, bottom = 8.dp, top = 16.dp),
            style = MaterialTheme.typography.bodySmall
          )
        }

        item {
          Card(
            modifier = Modifier
              .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = WalletColors.styleguide_blue_secondary)
          ) {
            LazyColumn(userScrollEnabled = false, modifier = Modifier.heightIn(0.dp, maxHeight)) {
              items(transactionsForDate) { transaction ->
                with(transaction.cardInfoByType()) {
                  TransactionCard(
                    icon = icon,
                    appIcon = appIcon,
                    title = stringResource(id = title),
                    description = description,
                    amount = amount,
                    convertedAmount = currency,
                    subIcon = subIcon,
                    onClick = { },
                    textDecoration = textDecoration
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

