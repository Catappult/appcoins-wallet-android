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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcoins.wallet.ui.common.R
import com.appcoins.wallet.ui.common.theme.WalletColors
import com.appcoins.wallet.ui.widgets.AddNewCardComposable
import com.asfoundation.wallet.manage_cards.models.StoredCard

@Composable
fun CardListBottomSheet(
  onChangeCardClick: () -> Unit,
  onAddNewCardClick: () -> Unit,
  onGotItClick: () -> Unit,
  cardList: List<StoredCard>
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
      .background(WalletColors.styleguide_blue_secondary),
  ) {
    LazyColumn(
      modifier = Modifier
        .padding(8.dp)
        .padding(horizontal = 16.dp)
    ) {
      item {
        AddNewCardComposable(
          onClickAction = onAddNewCardClick,
          addIconDrawable = com.asf.wallet.R.drawable.ic_add_card
        )
      }
      items(cardList) { card ->
        PaymentCardItem(card, onChangeCardClick)
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
      .padding(top = 8.dp)
      .fillMaxWidth()
      .height(56.dp)
      .clickable { onChangeCardClick }
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Image(
        modifier = Modifier
          .padding(16.dp)
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
    {},
    {},
    {},
    listOf(
      StoredCard("1234", com.asf.wallet.R.drawable.ic_card_brand_visa, null, false),
      StoredCard("4325", com.asf.wallet.R.drawable.ic_card_brand_american_express, null, true),
      StoredCard("1234", com.asf.wallet.R.drawable.ic_card_brand_discover, null, false)
    )
  )
}
