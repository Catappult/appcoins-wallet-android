package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
fun BalanceCardExpanded(
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
    colors = CardDefaults.cardColors(WalletColors.styleguide_blue_secondary),
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .clip(shape = RoundedCornerShape(8.dp))
  ) {
    if (newWallet) {
      BalanceCardNewUserExpanded(onClickTopUp = onClickTopUp)
    } else {
      Column {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            BalanceValue(balance, currencyCode, onClickCurrencies)
            Row {
              ButtonWithIcon(
                icon = R.drawable.ic_transfer,
                label = R.string.transfer_button,
                onClick = onClickTransfer,
                backgroundColor = WalletColors.styleguide_blue,
                labelColor = WalletColors.styleguide_white,
                iconColor = WalletColors.styleguide_pink,
                iconSize = 14.dp
              )
              Spacer(modifier = Modifier.padding(16.dp))
              ButtonWithIcon(
                icon = R.drawable.ic_topup,
                label = R.string.top_up_button,
                onClick = onClickTopUp,
                backgroundColor = WalletColors.styleguide_pink,
                labelColor = WalletColors.styleguide_white,
                iconColor = WalletColors.styleguide_white
              )
              Spacer(modifier = Modifier.padding(16.dp))
              VectorIconButton(
                imageVector = Icons.Default.MoreVert,
                contentDescription = R.string.action_more_details,
                onClick = onClickMenuOptions
              )
            }
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
          Column(modifier = Modifier.padding(16.dp)) { BackupAlertCardExpanded(onClickBackup) }
        }
      }
    }
  }
}


@Composable
private fun BalanceCardNewUserExpanded(onClickTopUp: () -> Unit) {
  Row(
    modifier = Modifier
      .padding(32.dp)
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(horizontalAlignment = Alignment.Start) {
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
        text = stringResource(id = R.string.intro_welcome_body),
        style =
        TextStyle(
          color = WalletColors.styleguide_white,
          fontSize = 14.sp,
        )
      )
    }
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


@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewLandscapeBalanceCard() {
  BalanceCardExpanded(
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

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewLandscapeBalanceCardWithoutBackup() {
  BalanceCardExpanded(
    balance = "€ 30.12",
    currencyCode = "Eur",
    onClickCurrencies = {},
    onClickTransfer = {},
    onClickBackup = {},
    onClickTopUp = {},
    onClickMenuOptions = {},
    showBackup = false,
    newWallet = false
  )
}

@Preview(device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PreviewLandscapeNewWalletBalanceCard() {
  BalanceCardExpanded(
    balance = "€ 30.12",
    currencyCode = "Eur",
    onClickCurrencies = {},
    onClickTransfer = {},
    onClickBackup = {},
    onClickTopUp = {},
    onClickMenuOptions = {},
    showBackup = true,
    newWallet = true,
  )
}
