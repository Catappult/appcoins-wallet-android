package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.R

@Composable
fun BalanceValue(
  balance: String,
  currencyCode: String,
  onClick: () -> Unit
) {
  Column {
    Row {
      Text(
        text = balance,
        color = WalletColors.styleguide_white,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
      )
      IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          painter = painterResource(id = R.drawable.ic_arrow_down_circle),
          contentDescription = stringResource(id = R.string.change_currency_title),
          tint = Color.Unspecified
        )
      }
    }
    Text(
      text = currencyCode.uppercase(),
      color = WalletColors.styleguide_dark_grey,
      fontSize = 12.sp
    )
  }
}

@Preview
@Composable
fun PreviewBalanceValue() {
  BalanceValue(
    balance = "€ 34.21",
    currencyCode = "EUR",
    onClick = {}
  )
}
