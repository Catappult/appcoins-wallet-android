package com.appcoins.wallet.ui.widgets.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
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
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.*

@Composable
fun BalanceValue(
  amount: BigDecimal,
  currency: Currency
) {
  Column {
    Row {
      Text(
        text = currency.symbol,
        color = WalletColors.styleguide_white,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = DecimalFormat("###.##").format(amount),
        color = WalletColors.styleguide_white,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
      )
      IconButton(
        onClick = {},
        modifier = Modifier.size(32.dp)
      ) { //TODO create "Change Currencies" screen
        Icon(
          painter = painterResource(id = R.drawable.ic_arrow_down_circle),
          contentDescription = stringResource(id = R.string.change_currency_title),
          tint = Color.Unspecified
        )
      }
    }
    Text(text = currency.currencyCode, color = WalletColors.styleguide_dark_grey, fontSize = 12.sp)
  }
}

@Preview
@Composable
fun PreviewBalanceValue() {
  BalanceValue(
    BigDecimal(349.12),
    Currency.getAvailableCurrencies().first { it.displayName.equals("Euro") }
  )
}
