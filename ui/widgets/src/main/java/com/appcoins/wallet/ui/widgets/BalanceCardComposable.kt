package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.BalanceValue
import com.appcoins.wallet.ui.widgets.component.ButtonWithIcon

@Composable
fun BalanceCard(
  balance: String,
  currencyCode: String,
  onClickCurrencies: () -> Unit,
  onClickTransfer: () -> Unit,
  onClickTopUp: () -> Unit,
  onClickBackup: () -> Unit,
  onClickMenuOptions: () -> Unit,
  showBackup: Boolean = true,
  newWallet: Boolean = true
) {
  Card(
    backgroundColor = WalletColors.styleguide_blue_secondary,
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .clip(shape = RoundedCornerShape(8.dp))
  ) {
    if (newWallet) {
      BalanceCardNewUser(onClickTopUp = onClickTopUp)
    } else {
      Column {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            BalanceValue(balance, currencyCode, onClickCurrencies)
            VectorIconButton(
              imageVector = Icons.Default.MoreVert,
              contentDescription = R.string.action_more_details,
              onClick = onClickMenuOptions
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            ButtonWithIcon(
              icon = R.drawable.ic_transfer,
              label = R.string.transfer_button,
              onClick = onClickTransfer,
              backgroundColor = WalletColors.styleguide_blue,
              labelColor = WalletColors.styleguide_white,
              iconColor = WalletColors.styleguide_pink
            )
            ButtonWithIcon(
              icon = R.drawable.ic_topup,
              label = R.string.top_up_button,
              onClick = onClickTopUp,
              backgroundColor = WalletColors.styleguide_pink,
              labelColor = WalletColors.styleguide_white,
              iconColor = WalletColors.styleguide_white
            )
          }
        }
        if (showBackup) {
          Surface(
            modifier =
            Modifier
              .fillMaxWidth()
              .absolutePadding(top = 4.dp, bottom = 4.dp)
              .size(1.dp),
            color = WalletColors.styleguide_blue,
            content = {})
          Column(modifier = Modifier.padding(16.dp)) { BackupAlertCard(onClickBackup) }
        }
      }
    }
  }
}

@Composable
fun BalanceCardNewUser(onClickTopUp: () -> Unit) {
  Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = stringResource(id = R.string.intro_welcome_header),
      style =
      TextStyle(
        color = WalletColors.styleguide_white,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
    )
    Text(
      modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
      text = stringResource(id = R.string.intro_welcome_body),
      style = TextStyle(color = WalletColors.styleguide_white, fontSize = 14.sp)
    )
    ButtonWithIcon(
      icon = R.drawable.ic_topup,
      label = R.string.top_up_button,
      onClick = onClickTopUp,
      backgroundColor = WalletColors.styleguide_pink,
      labelColor = WalletColors.styleguide_white,
      iconColor = WalletColors.styleguide_white
    )
  }
}

@Preview
@Composable
fun PreviewBalanceCard() {
  BalanceCard(
    balance = "€ 30.12",
    currencyCode = "Eur",
    onClickCurrencies = {},
    onClickTransfer = {},
    onClickBackup = {},
    onClickTopUp = {},
    onClickMenuOptions = {},
    showBackup = true,
    newWallet = false
  )
}
