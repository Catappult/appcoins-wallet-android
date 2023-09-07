package com.appcoins.wallet.feature.personalinfo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletTypography
import com.appcoins.wallet.ui.widgets.TopBar

@Composable
fun PersonalInformationRoute(onChatClick: () -> Unit) {
  Scaffold(
    topBar = { Surface { TopBar(isMainBar = false, onClickSupport = { onChatClick() }) } },
    modifier = Modifier
  ) { padding ->
    PersonalInformationScreen(scaffoldPadding = padding)
  }
}

@Composable
fun PersonalInformationScreen(scaffoldPadding: PaddingValues) {
  Column {
    PersonalInformationList(scaffoldPadding)
  }
}


@Composable
private fun PersonalInformationList(
  scaffoldPadding: PaddingValues
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = scaffoldPadding.calculateTopPadding())
      .padding(horizontal = 16.dp)
  ) {
    item {
      Text(
        text = "Personal",//TODO change to string
        style = WalletTypography.bold.sp22,
        modifier = Modifier
          .padding(horizontal = 16.dp, vertical = 16.dp),
      )
    }
  }
}

