package com.appcoins.wallet.ui.widgets

import androidx.appcompat.widget.MenuPopupWindow.MenuDropDownListView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.component.BalanceValue
import com.appcoins.wallet.ui.widgets.component.ButtonWithIcon
import java.math.BigDecimal
import java.util.*

@Composable
fun BalanceCard(
  balance: BigDecimal,
  currency: Currency,
  menuItems: List<MenuDropDownListView>,
  onClickTransfer: () -> Unit,
  onClickTopUp: () -> Unit,
  onClickBackup: (() -> Unit)? = null,
) {
  Card(
    backgroundColor = WalletColors.styleguide_blue_secondary,
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .clip(shape = RoundedCornerShape(8.dp))
  ) {
    Column {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          BalanceValue(balance, currency)
          VectorIconButton(
            imageVector = Icons.Default.MoreVert,
            contentDescription = R.string.action_more_details,
            onClick = {}) //TODO show $menuItems
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

      if (onClickBackup != null) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .absolutePadding(top = 4.dp, bottom = 4.dp)
            .size(1.dp),
          color = WalletColors.styleguide_blue,
          content = {})
        Column(modifier = Modifier.padding(16.dp)) {
          BackupAlertCard(onClickBackup)
        }
      }
    }
  }
}

@Preview
@Composable
fun PreviewBalanceCard() {
  BalanceCard(
    balance = BigDecimal(30.12),
    currency = Currency.getAvailableCurrencies().first { it.displayName.equals("Euro") },
    menuItems = listOf(),
    onClickTransfer = {},
    onClickBackup = {},
    onClickTopUp = {}
  )
}
