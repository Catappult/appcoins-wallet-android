package com.asfoundation.wallet.topup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.AddNewCardComposable
import com.asfoundation.wallet.manage_cards.models.StoredCard

@Composable
fun CardListBottomSheet(
  onChangeCardClick: (StoredCard, () -> Unit) -> Unit,
  onAddNewCardClick: () -> Unit,
  onGotItClick: () -> Unit,
  cardList: List<StoredCard>,
  isGotItVisible: Boolean
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .background(WalletColors.styleguide_blue_secondary),
  ) {
    LazyColumn {
      item {
        AddNewCardComposable(
          paddingTop = 8.dp,
          paddingBottom = 8.dp,
          paddingEnd = 8.dp,
          paddingStart = 8.dp,
          cardHeight = 56.dp,
          imageEndPadding = 16.dp,
          imageSize = 36.dp,
          onClickAction = onAddNewCardClick,
          addIconDrawable = com.asf.wallet.R.drawable.ic_add_card,
          titleText = stringResource(R.string.manage_cards_settings_add_title),
          backgroundColor = WalletColors.styleguide_blue_secondary,
          textColor = WalletColors.styleguide_light_grey
        )
      }
      items(cardList) { card ->
        PaymentCardItem(card) { onChangeCardClick(card) {} }
      }
      item {
        Column {
          Spacer(modifier = Modifier.height(16.dp))
          if (isGotItVisible) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(WalletColors.styleguide_blue),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                  text = stringResource(R.string.manage_cards_update_disclaimer_1),
                  color = WalletColors.styleguide_dark_grey,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = stringResource(R.string.manage_cards_update_disclaimer_2),
                  color = WalletColors.styleguide_white,
                  fontSize = 12.sp
                )
              }
              TextButton(onClick = onGotItClick, modifier = Modifier.padding(end = 8.dp)) {
                Text(
                  text = stringResource(id = R.string.got_it_button),
                  fontWeight = FontWeight.Bold,
                  color = WalletColors.styleguide_pink,
                  fontSize = 14.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun PaymentCardItem(storedCard: StoredCard, onChangeCardClick: () -> Unit) {
  val containerColor =
    if (storedCard.isSelectedCard) WalletColors.styleguide_blue else WalletColors.styleguide_blue_secondary
  Card(
    colors = CardDefaults.cardColors(containerColor = containerColor),
    modifier = Modifier
      .padding(8.dp)
      .fillMaxWidth()
      .height(56.dp)
      .clickable { onChangeCardClick() }
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Image(
        modifier = Modifier
          .padding(16.dp)
          .width(36.dp)
          .align(Alignment.CenterVertically),
        painter = painterResource(storedCard.cardIcon),
        contentDescription = "Card icon",
      )
      Text(
        text = "**** ".plus(storedCard.cardLastNumbers),
        modifier = Modifier
          .padding(8.dp)
          .align(Alignment.CenterVertically),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = WalletColors.styleguide_light_grey,
      )
      Spacer(modifier = Modifier.weight(1f))
      if (storedCard.isSelectedCard) {
        Image(
          modifier = Modifier
            .align(Alignment.CenterVertically)
            .padding(end = 16.dp)
            .size(12.dp),
          colorFilter = ColorFilter.tint(Color.Green),
          painter = painterResource(R.drawable.ic_check_mark),
          contentDescription = "Selected Card",
        )
      }
    }
  }
}

@Preview
@Composable
fun PreviewBackupDialogCardAlertBottomSheet() {
  CardListBottomSheet(
    { _, _ -> {} },
    {},
    {},
    listOf(
      StoredCard("1234", com.asf.wallet.R.drawable.ic_card_brand_visa, null, false),
      StoredCard("4325", com.asf.wallet.R.drawable.ic_card_brand_american_express, null, true),
      StoredCard("1234", com.asf.wallet.R.drawable.ic_card_brand_discover, null, false)
    ),
    true
  )
}
