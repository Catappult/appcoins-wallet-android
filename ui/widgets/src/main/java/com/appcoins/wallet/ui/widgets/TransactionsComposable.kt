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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_blue_secondary
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_dark_grey
import com.appcoins.wallet.ui.common.theme.WalletColors.styleguide_light_grey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
  icon: Int?,
  appIcon: String?,
  title: String,
  description: String?,
  amount: String?,
  currency: String?,
  subIcon: Int?,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = styleguide_blue_secondary),
    onClick = onClick
  ) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        TransactionItem(icon, appIcon, subIcon)
        Spacer(modifier = Modifier.padding(14.dp))
        Column(modifier = Modifier.widthIn(0.dp, 168.dp)) {
          Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = styleguide_light_grey,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (description != null) Text(
            text = description,
            color = styleguide_dark_grey,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
      Column(horizontalAlignment = Alignment.End) {
        if (amount != null) Text(
          text = amount,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.End,
          color = styleguide_light_grey,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (currency != null) Text(
          text = currency,
          color = styleguide_dark_grey,
          style = MaterialTheme.typography.bodySmall,
          textAlign = TextAlign.End,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun TransactionItem(icon: Int?, appIcon: String?, subIcon: Int?) {
  Box(contentAlignment = Alignment.BottomEnd) {
    if (icon != null) Icon(
      painter = painterResource(id = icon),
      contentDescription = null,
      tint = Color.Unspecified,
      modifier = Modifier.size(40.dp)
    ) else {
      SubcomposeAsyncImage(
        model = appIcon,
        contentDescription = "Game Icon",
        modifier = Modifier.size(40.dp),
        contentScale = ContentScale.Crop,
        loading = {
          CircularProgressIndicator()
        }
      )
    }
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
    icon = null,
    appIcon = "",
    title = "Reverted Purchase Bonus test used to verify UI",
    description = "AppCoins Trivial demo sample used to test the UI",
    amount = "-12,21238745674839837456.73",
    currency = "-12,5000000000000000000.00 APPC-C",
    subIcon = R.drawable.ic_transaction_reverted_mini
  ) { }
}
