package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_medium_grey

@Composable
fun TransactionCard(
  icon: Painter,
  title: String,
  description: String?,
  amount: String?,
  currency: String?,
  subIcon: Int?
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = styleguide_blue_secondary),
  ) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        TransactionItem(icon, subIcon)
        Spacer(modifier = Modifier.padding(14.dp))
        Column(modifier = Modifier.widthIn(0.dp, 168.dp)) {
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = styleguide_light_grey,
            style = MaterialTheme.typography.bodyMedium
          )
          if (description != null) Text(
            text = description,
            color = styleguide_medium_grey,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
      Column(horizontalAlignment = Alignment.End) {
        if (amount != null) Text(
          text = amount,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyMedium,
          color = styleguide_light_grey
        )
        if (currency != null) Text(
          text = currency,
          color = styleguide_medium_grey,
          style = MaterialTheme.typography.bodySmall
        )
      }
    }
  }
}

@Composable
fun TransactionItem(icon: Painter, subIcon: Int?) {
  Box(contentAlignment = Alignment.BottomEnd) {
    Icon(
      painter = icon,
      contentDescription = null,
      tint = Color.Unspecified,
      modifier = Modifier.size(40.dp)
    )
    if (subIcon != null) Icon(
      painter = painterResource(subIcon),
      contentDescription = null,
      tint = Color.Unspecified,
      modifier = Modifier.size(24.dp)
    )
  }

}

@Preview
@Composable
fun PreviewTransactionCard() {
  TransactionCard(
    icon = painterResource(id = R.drawable.ic_transaction_reverted_reward),
    title = "Reverted Purchase Bonus",
    description = "The Bonus you received on Mar, 14 2022 has been reverted.",
    amount = "-12.73",
    currency = "-30.45 APPC-C",
    subIcon = R.drawable.ic_transaction_reverted_mini
  )
}
