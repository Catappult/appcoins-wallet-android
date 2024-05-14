package com.asfoundation.wallet.billing.adyen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun CardListExpandedScreen(
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
      .clip(RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp))
      .background(WalletColors.styleguide_white),
  ) {
    Box(modifier = Modifier.weight(1f)) {
      LazyColumn(
        modifier = Modifier
          .padding(top = 0.dp, bottom = 0.dp, start = 8.dp, end = 8.dp)
      ) {
        item {
          AddNewCardComposable(
            paddingTop = 2.dp,
            onClickAction = onAddNewCardClick,
            addIconDrawable = com.asf.wallet.R.drawable.ic_add_card,
            titleText = stringResource(R.string.manage_cards_settings_add_title),
            backgroundColor = WalletColors.styleguide_white,
            textColor = WalletColors.styleguide_black
          )
        }
        items(cardList) { card ->
          PaymentCardItem(card) { onChangeCardClick(card) {} }
        }
      }
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .height(32.dp)
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Transparent,
                WalletColors.styleguide_white_75
              )
            )
          )
      )
    }
    if (isGotItVisible) {
      Card(
        colors = CardDefaults.cardColors(WalletColors.styleguide_light_grey),
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)

      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(WalletColors.styleguide_light_grey),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
              text = stringResource(id = R.string.manage_cards_update_disclaimer_1),
              color = WalletColors.styleguide_dark_grey,
              fontSize = 12.sp
            )
            Text(
              text = stringResource(id = R.string.manage_cards_update_disclaimer_2),
              color = WalletColors.styleguide_black,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
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

@Composable
fun PaymentCardItem(storedCard: StoredCard, onChangeCardClick: () -> Unit) {
  val containerColor =
    if (storedCard.isSelectedCard) WalletColors.styleguide_light_grey else WalletColors.styleguide_white
  Card(
    colors = CardDefaults.cardColors(containerColor = containerColor),
    modifier = Modifier
      .padding(top = 4.dp)
      .fillMaxWidth()
      .height(40.dp)
      .clickable { onChangeCardClick() }
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Image(
        modifier = Modifier
          .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp)
          .height(20.dp)
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
        color = WalletColors.styleguide_black,
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
  CardListExpandedScreen(
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
