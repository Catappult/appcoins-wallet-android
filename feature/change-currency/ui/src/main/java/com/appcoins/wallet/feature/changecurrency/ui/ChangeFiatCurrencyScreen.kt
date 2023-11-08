package com.appcoins.wallet.feature.changecurrency.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.feature.changecurrency.data.ChangeFiatCurrency
import com.appcoins.wallet.feature.changecurrency.data.FiatCurrency
import com.appcoins.wallet.feature.changecurrency.ui.bottomsheet.ChooseCurrencyRoute
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.common.theme.WalletTheme
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.common.theme.shapes
import com.appcoins.wallet.ui.widgets.TopBar
import com.appcoins.wallet.ui.widgets.WalletImage
import kotlinx.coroutines.launch
import com.appcoins.wallet.ui.common.R as CommonR

@Composable
fun ChangeFiatCurrencyRoute(
  onExitClick: () -> Unit,
  onChatClick: () -> Unit,
  viewModel: ChangeFiatCurrencyViewModel = hiltViewModel(),
) {
  val changeFiatCurrencyState by viewModel.stateFlow.collectAsState()
  Scaffold(
    topBar = {
      Surface {
        TopBar(isMainBar = false, onClickSupport = { onChatClick() })
      }
    },
    modifier = Modifier
  ) { padding ->
    ChangeFiatCurrencyScreen(
      changeFiatCurrencyState = changeFiatCurrencyState,
      scaffoldPadding = padding,
      onExitClick = onExitClick
    )
  }
}

@Composable
fun ChangeFiatCurrencyScreen(
  scaffoldPadding: PaddingValues,
  changeFiatCurrencyState: ChangeFiatCurrencyState,
  onExitClick: () -> Unit
) {
  when (changeFiatCurrencyState.changeFiatCurrencyAsync) {
    Async.Uninitialized,
    is Async.Loading -> Unit
    is Async.Success -> ChangeFiatCurrencyList(
      scaffoldPadding = scaffoldPadding,
      model = changeFiatCurrencyState.changeFiatCurrencyAsync.value!!,
      onExitClick = onExitClick
    )
    is Async.Fail -> Unit
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeFiatCurrencyList(
  scaffoldPadding: PaddingValues,
  model: ChangeFiatCurrency,
  onExitClick: () -> Unit
) {
  val selectedItem =
    model.list.find { fiatCurrency -> fiatCurrency.currency == model.selectedCurrency }
  val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = scaffoldPadding.calculateTopPadding())
      .padding(horizontal = 16.dp)
  ) {
    item {
      Text(
        text = stringResource(id = CommonR.string.change_currency_title),
        style = WalletTypography.bold.sp22,
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 16.dp),
      )
    }
    selectedItem?.let {
      item {
        CurrencyItem(
          currencyItem = selectedItem,
          isSelected = true,
          onExitClick = onExitClick,
          bottomSheetState = bottomSheetState,
        )
      }
    }
    items(model.list) { currencyItem ->
      if (selectedItem != currencyItem) {
        CurrencyItem(
          currencyItem = currencyItem,
          onExitClick = onExitClick,
          bottomSheetState = bottomSheetState,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyItem(
  currencyItem: FiatCurrency,
  isSelected: Boolean = false,
  onExitClick: () -> Unit,
  bottomSheetState: SheetState
) {
  var openBottomSheet by rememberSaveable { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  var chosenCurrency: FiatCurrency? by rememberSaveable { mutableStateOf(null) }

  Card(
    shape = shapes.large,
    border = if (isSelected) BorderStroke(1.dp, WalletColors.styleguide_pink) else null,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 8.dp),
    onClick = {
      chosenCurrency = currencyItem
      openBottomSheet = !openBottomSheet
    }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      WalletImage(
        data = currencyItem.flag,
        modifier = Modifier
          .clip(CircleShape)
          .size(50.dp)
      )
      Column(
        modifier = Modifier
          .padding(start = 16.dp)
      ) {
        Text(
          text = currencyItem.currency,
          style = WalletTypography.medium.sp22
        )
        Text(
          text = currencyItem.label!!,
          style = WalletTypography.medium.sp14
        )
      }
      if (isSelected) {
        Spacer(modifier = Modifier.weight(1f))
        WalletImage(
          data = CommonR.drawable.ic_check_mark_pink,
          contentScale = ContentScale.FillWidth,
          modifier = Modifier
            .size(16.dp)
        )
      }
    }
  }
  if (openBottomSheet) {
    ModalBottomSheet(
      onDismissRequest = {
        openBottomSheet = false
        chosenCurrency = null
      },
      sheetState = bottomSheetState,
      containerColor = WalletColors.styleguide_blue
    ) {
      chosenCurrency?.let {
        ChooseCurrencyRoute(
          chosenCurrency = chosenCurrency,
          bottomSheetStateHandle = {
            scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
              openBottomSheet = !openBottomSheet
              chosenCurrency = null
              onExitClick()
            }
          },
        )
      }
    }
  }
}

@Preview("ChangeFiatCurrencyList")
@Composable
private fun ChangeFiatCurrencyListPreview() {
  WalletTheme(darkTheme = false) {
    ChangeFiatCurrencyList(
      model = ChangeFiatCurrency(
        selectedCurrency = "EUR",
        list = listOf(
          FiatCurrency(
            currency = "EUR",
            label = "Euro",
            flag = "https://upload.wikimedia.org/wikipedia/commons/b/b7/Flag_of_Europe.svg",
            sign = "€",
          ),
          FiatCurrency(
            currency = "USD",
            label = "United States Dollar",
            flag = "https://upload.wikimedia.org/wikipedia/commons/b/b7/Flag_of_Europe.svg",
            sign = "$",
          )
        )
      ),
      onExitClick = {},
      scaffoldPadding = PaddingValues(0.dp)
    )
  }
}
