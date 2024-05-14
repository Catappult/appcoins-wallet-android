package com.appcoins.wallet.ui.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    0.dp,
    0.dp,
    0.dp,
    56.dp,
    16.dp,
    36.dp,
    {},
    R.drawable.ic_plus,
    stringResource(R.string.manage_cards_add_title),
    WalletColors.styleguide_blue_secondary,
    WalletColors.styleguide_light_grey
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewCardComposable(
  paddingTop: Dp,
  paddingBottom: Dp,
  paddingEnd: Dp,
  paddingStart: Dp,
  cardHeight: Dp,
  imageEndPadding: Dp,
  imageSize: Dp,
  onClickAction: () -> Unit,
  addIconDrawable: Int,
  titleText: String,
  backgroundColor: Color,
  textColor: Color,
) {
  Card(
    onClick = onClickAction,
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    modifier = Modifier
      .padding(top = paddingTop, bottom = paddingBottom, end = paddingEnd, start = paddingStart)
      .fillMaxWidth()
      .height(cardHeight)
  ) {
    Row(
      modifier =
      Modifier.fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Image(
        modifier = Modifier
          .padding(start = 16.dp, end = imageEndPadding)
          .width(imageSize)
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
        color = textColor,
      )
    }
  }
}
