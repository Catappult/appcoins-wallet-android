package com.asfoundation.wallet.wallet_v3.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.home.R
import com.appcoins.wallet.ui.widgets.TopBar


@Composable
fun HomeScreenRoute(
  modifier: Modifier = Modifier,
//  viewModel: HomeViewModel by viewModels(),  //TODO
) {
  HomeScreen(
    modifier = modifier,
  )
}

@Composable
fun HomeScreen(
  modifier: Modifier = Modifier,
) {
  val scaffoldState = rememberScaffoldState()
  Scaffold(
    scaffoldState = scaffoldState,
    topBar = {
      Surface(elevation = 4.dp) {
        TopBar(
          isMainBar = true,
          isVip = false,
          onClickNotifications = { Log.d("TestHomeFragment", "Notifications")},
          onClickSettings = { Log.d("TestHomeFragment", "Settings")},
          onClickSupport = { Log.d("TestHomeFragment", "Support")},
        )}
             },
    backgroundColor = colorResource(R.color.styleguide_blue),
    modifier = modifier
  ) { padding ->
      HomeScreenContent(
        padding = padding
      )
    }
}

@Composable
internal fun HomeScreenContent(
  padding: PaddingValues
) {
  Column(
    modifier = Modifier
      .verticalScroll(rememberScrollState())
      .padding(padding),
  ) {
    DummyCard()
    DummyCard()
    DummyCard()
    DummyCard()
    DummyCard()
    }
}

@Composable
fun DummyCard() {
  Card(
    modifier = Modifier
      .padding(
        start = 16.dp,
        end = 16.dp,
        top = 16.dp
      )
      .fillMaxWidth()
      .height(200.dp),
    shape = RoundedCornerShape(8.dp),
    backgroundColor = colorResource(R.color.styleguide_blue_secondary),
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Home Screen",
        style = MaterialTheme.typography.h5,
        color = colorResource(R.color.styleguide_white)
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
  HomeScreen()
}
