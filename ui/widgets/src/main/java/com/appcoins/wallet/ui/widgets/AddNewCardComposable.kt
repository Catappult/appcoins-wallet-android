package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.theme.WalletColors


@Preview
@Composable
private fun AddNewCardComposableExample() {
  AddNewCardComposable(
    12.dp,
    {},
    R.drawable.ic_card,
    stringResource(R.string.manage_cards_add_title)
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewCardComposable(
  paddingTop: Dp,
  onClickAction: () -> Unit,
  addIconDrawable: Int,
  titleText: String
) {
  Card(
    onClick = onClickAction,
    colors = CardDefaults.cardColors(containerColor = WalletColors.styleguide_blue_secondary),
    modifier = Modifier
      .padding(top = paddingTop)
      .fillMaxWidth()
      .height(56.dp)
  ) {
    Row {
      Image(
        modifier = Modifier
          .padding(16.dp)
          .align(Alignment.CenterVertically),
        painter = painterResource(addIconDrawable),
        contentDescription = stringResource(R.string.title_support),
      )
      Text(
        text = titleText,
        modifier = Modifier
          .padding(8.dp)
          .align(Alignment.CenterVertically),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = WalletColors.styleguide_light_grey,
      )
    }
  }
}
