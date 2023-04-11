package com.asfoundation.wallet.change_currency

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.appcoins.wallet.ui.arch.data.Async
import com.appcoins.wallet.ui.common.R as CommonR


@Composable
internal fun NavigationGraph(
  onCurrencyClick: (String) -> Unit,
  viewModel: ChangeFiatCurrencyViewModel = hiltViewModel()
) {
  val changeFiatCurrencyState by viewModel.stateFlow.collectAsState()
  ChangeFiatCurrencyScreen(
    changeFiatCurrencyState = changeFiatCurrencyState,
    onCurrencyClick = onCurrencyClick
  )
}

@Composable
internal fun ChangeFiatCurrencyScreen(
  changeFiatCurrencyState: ChangeFiatCurrencyState,
  onCurrencyClick: (String) -> Unit
) {
  when (changeFiatCurrencyState.changeFiatCurrencyAsync) {
    Async.Uninitialized,
    is Async.Loading -> TODO()
    is Async.Success -> ChangeFiatCurrencyList(
      currencyList = changeFiatCurrencyState.changeFiatCurrencyAsync.value!!.list,
      onCurrencyClick = onCurrencyClick
    )
    is Async.Fail -> TODO()
  }

}

@Composable
private fun ChangeFiatCurrencyList(
  currencyList: List<FiatCurrencyEntity>,
  onCurrencyClick: (String) -> Unit
) {
  LazyColumn(
    content = {
      items(currencyList) { currencyItem ->
        CurrencyItem(
          currencyItem = currencyItem,
          onCurrencyClick = onCurrencyClick,
        )
      }
    })
}

@Composable
fun CurrencyItem(
  currencyItem: FiatCurrencyEntity,
  onCurrencyClick: (String) -> Unit,
) {
  Row(
    modifier = Modifier
      .clickable { onCurrencyClick(currencyItem.currency) }
      .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(painter = painterResource(id = CommonR.drawable.currency_flag_placeholder), contentDescription =  null )
//    AsyncImage(
//      placeholder = painterResource(R.drawable.currency_flag_placeholder),
//      model = currencyItem.flag,
//      contentScale = ContentScale.Crop,
//      contentDescription = null,
//      modifier = Modifier
//        .size(40.dp)
//        .padding(end = 16.dp)
//    )
    Text(
      text = currencyItem.label!!,
    )
  }
}

@Preview("ChangeFiatCurrencyList")
@Composable
private fun ChangeFiatCurrencyListPreview() {
  ChangeFiatCurrencyList(
    currencyList = listOf(
      FiatCurrencyEntity(
        currency = "EUR",
        label = "Euro",
        flag = "https://upload.wikimedia.org/wikipedia/commons/b/b7/Flag_of_Europe.svg",
        sign = "€",
      ),
      FiatCurrencyEntity(
        currency = "EUR",
        label = "Euro",
        flag = "https://upload.wikimedia.org/wikipedia/commons/b/b7/Flag_of_Europe.svg",
        sign = "€",
      )
    ),
    onCurrencyClick = {},
  )
}
